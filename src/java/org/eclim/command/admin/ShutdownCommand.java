/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.command.admin;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimApplication;

import org.eclim.logging.Logger;

/**
 * Command to shutdown the eclim server.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ShutdownCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(ShutdownCommand.class);

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    try{
      EclimApplication.getInstance().stop();
    }catch(Exception e){
      logger.error("Error shutting down eclim:", e);
    }
    return null;
  }
}
