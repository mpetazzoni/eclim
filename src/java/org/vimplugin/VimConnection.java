/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashSet;

import java.util.regex.Pattern;

import org.vimplugin.listeners.FeedKeys;
import org.vimplugin.listeners.FileOpened;
import org.vimplugin.listeners.FileUnmodified;
import org.vimplugin.listeners.IVimListener;
import org.vimplugin.listeners.Logger;
import org.vimplugin.listeners.ServerDisconnect;
import org.vimplugin.listeners.ServerStarted;
import org.vimplugin.listeners.TextInsert;
import org.vimplugin.listeners.TextRemoved;

import org.vimplugin.preferences.PreferenceConstants;

/**
 * Manage the communication channel with Vim. This is the main interface to a
 * Vim instance. Important functions are: start/close tcp communication, and
 * sending of commands/functions. The protocol is explained in detail at vimdoc.
 * Events generated by Vim are consumed by
 * {@link org.vimplugin.listeners.IVimListener Listeners} (ObserverPattern).
 *
 * @see <a
 *      href="http://www.vim.org/htmldoc/netbeans.html#netbeans-protocol">Protocol
 *      specification</a>
 *
 */
public class VimConnection implements Runnable
{
  private static final org.eclim.logging.Logger logger =
    org.eclim.logging.Logger.getLogger(VimConnection.class);

  /* pattern to match events lines to differencient from function response while
     waiting on function response */
  private static final Pattern EVENT = Pattern.compile("^\\d+:\\w+=\\d+.*");

  /** is a vim instance running? */
  private boolean serverRunning = false;

  /** did the vim instance report "startupDone"? */
  private boolean startupDone = false;

  /** the set of VimListeners. Observer-Pattern. */
  final HashSet<IVimListener> listeners = new HashSet<IVimListener>();

  /** the id of the calling vim instance (as given in VimServer) */
  final int vimID;

  /** the channel we can get messages from */
  private BufferedReader in;

  /** the channel we can write messages to */
  private PrintWriter out;

  private final int port;

  private ServerSocket socket;

  /** the socket the vim instance runs on */
  private Socket vimSocket;

  private volatile boolean functionCalled = false;
  private Object functionMonitor = new Object();
  private String functionResult;

  /** creates a connection object (but does not start the connection ..). */
  public VimConnection(int instanceID) {
    port = VimPlugin.getDefault().getPreferenceStore().getInt(
        PreferenceConstants.P_PORT);
    vimID = instanceID;

  }

  /**
   * Establishes a TCP-Connection, adds
   * {@link org.vimplugin.listeners.IVimListener listeners} and creates VimEvents to be
   * consumed by the listeners.
   *
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      // start server
      socket = new ServerSocket(port + vimID);
      logger.debug("Server started and listening");

      // accept client
      vimSocket = socket.accept();
      out = new PrintWriter(vimSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(vimSocket
          .getInputStream()));
      logger.debug("Connection established");

      // Add Listeners
      listeners.add(new Logger());
      listeners.add(new ServerStarted());
      listeners.add(new ServerDisconnect());
      listeners.add(new TextInsert());
      listeners.add(new TextRemoved());
      listeners.add(new FileOpened());
      listeners.add(new FileUnmodified());
      listeners.add(new FeedKeys());

      // handle Events
      String line;

      try {
        while (!startupDone && (line = in.readLine()) != null) {
          //ignore "special messages" (see :help nb-special)
          if (!line.startsWith("AUTH")) {
            VimEvent ve = new VimEvent(line, this);
            for (IVimListener listener : listeners) {
              listener.handleEvent(ve);
            }
          }
        }
      } catch (VimException ve) {
        // TODO : better ErrorHandling (Connection Thread)
        ve.printStackTrace();
      }

      try {
        while ((serverRunning && (line = in.readLine()) != null)) {
          if(functionCalled && !EVENT.matcher(line).matches()){
            synchronized(functionMonitor){
              functionResult = line;
              functionMonitor.notify();
            }
          }else{
            VimEvent ve = new VimEvent(line, this);
            for (IVimListener listener : listeners) {
              listener.handleEvent(ve);
            }
          }
        }
      } catch (VimException ve) {
        // TODO : better ErrorHandling (Connection Thread)
        ve.printStackTrace();
      }
    } catch (IOException e) {
      // TODO: better ErrorHandling (Connection Thread)
      e.printStackTrace();
    }
  }

  /**
   * shuts down the TCP-Connection to the vim instance.
   *
   * @return always true.
   */
  public boolean close() throws IOException {
    vimSocket.close();
    socket.close();
    serverRunning = false;
    return true;
  }

  /**
   * Sends a <i>command</i> (no replay) as specified by the netbeans-protocol
   * to the vim instance.
   *
   * @param bufID the vim buffer that is adressed
   * @param name the "name" of the command
   * @param param possible parameters
   * @see <a
   *      href="http://www.vim.org/htmldoc/netbeans.html#netbeans-protocol">Protocol
   *      specification</a>
   */
  public void command(int bufID, String name, String param) {
    int seqno = VimPlugin.getDefault().nextSeqNo();
    String cmd = bufID + ":" + name + "!" + seqno + " " + param;
    logger.debug("command: " + cmd);
    out.println(cmd);
  }

  /**
   * Sends a <i>function</i> (reply with a String as return value) as
   * specified by the netbeans-protocol to the vim instance.
   *
   * @param bufID the vim buffer that is adressed
   * @param name the "name" of the command
   * @param param possible parameters
   * @return the return value of the (vim)function or "goodbye" if the function was "saveAndExit".
   * @see <a
   *      href="http://www.vim.org/htmldoc/netbeans.html#netbeans-protocol">Protocol
   *      specification</a>
   */
  public String function(int bufID, String name, String param)
    throws IOException
  {
    int seqno = VimPlugin.getDefault().nextSeqNo();
    String tmp = bufID + ":" + name + "/" + seqno + " " + param;
    logger.debug("function: " + tmp);

    if ("saveAndExit".equals(name)){
      out.println(tmp);
      return null;
    }

    // FIXME: need to find a better way to handle reading of function result
    // while run() is continously reading _all_ input from gvim.
    // FIXME: make use of the seqno for recognizing a function response.
    synchronized(functionMonitor){
      functionCalled = true;
      out.println(tmp);
      try{
        functionMonitor.wait(1000);
      }catch(InterruptedException ie){
        logger.error("Interrupted while waiting for function result.");
        return null;
      }
    }

    String result = functionResult;
    functionResult = null;
    logger.debug("result: " + result);
    return result;
  }

  /**
   * Sends a plain string to the vim instance. The user is responsible to
   * comply to the protocol syntax.
   *
   * @param s the string to send.
   * @see <a
   *      href="http://www.vim.org/htmldoc/netbeans.html#netbeans-protocol">Protocol
   *      specification</a>
   */
  public void plain(String s) {
    logger.debug("plain: " + s);
    out.println(s);
  }

  /**
   * Adds a Listener to the list of observers. On each event all listeners are
   * informed about the event and may react to it. (Observer-Pattern).
   *
   * @param vl the new listener.
   */
  public void addListener(IVimListener vl) {
    listeners.add(vl);
  }

  /**
   * Simple Getter.
   *
   * @return the id of this connection
   */
  public int getVimID() {
    return vimID;
  }

  /**
   * Simple Setter.
   *
   * @param startupDone
   */
  public void setStartupDone(boolean startupDone) {
    this.startupDone = startupDone;
  }

  /**
   * Simple Getter.
   *
   * @return did Vim threw already "startupDone" Message?
   */
  public boolean isStartupDone() {
    return startupDone;
  }

  /**
   * Simple Setter.
   *
   * @param serverRunning
   */
  public void setServerRunning(boolean serverRunning) {
    this.serverRunning = serverRunning;
  }

  /**
   * Simple getter.
   *
   * @return whether a vim instance is running.
   */
  public boolean isServerRunning() {
    return serverRunning;
  }
}
