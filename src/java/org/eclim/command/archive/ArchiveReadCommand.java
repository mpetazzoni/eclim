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
package org.eclim.command.archive;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

/**
 * Command to read a file from a commons vfs compatable path.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ArchiveReadCommand
  extends AbstractCommand
{
  private static final String URI_PREFIX = "file://";
  private static final Pattern WIN_PATH = Pattern.compile("^/[a-zA-Z]:/.*");

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    InputStream in = null;
    OutputStream out = null;
    FileSystemManager fsManager = null;
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);

      fsManager = VFS.getManager();
      FileObject fileObject = fsManager.resolveFile(file);
      FileObject tempFile = fsManager.resolveFile(
          SystemUtils.JAVA_IO_TMPDIR + fileObject.getName().getPath());

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(fileObject.getFileSystem());
      //fsManager.getFilesCache().clear(tempFile.getFileSystem());

      // NOTE: FileObject.getName().getPath() does not include the drive
      // information.
      String path = tempFile.getName().getURI().substring(URI_PREFIX.length());
      // account for windows uri which has an extra '/' in front of the drive
      // letter (file:///C:/blah/blah/blah).
      if (WIN_PATH.matcher(path).matches()){
        path = path.substring(1);
      }

      if(!tempFile.exists()){
        tempFile.createFile();

        in = fileObject.getContent().getInputStream();
        out = tempFile.getContent().getOutputStream();
        IOUtils.copy(in, out);

        new File(path).deleteOnExit();
      }

      return path;
    }finally{
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
