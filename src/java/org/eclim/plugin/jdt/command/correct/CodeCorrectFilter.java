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
package org.eclim.plugin.jdt.command.correct;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for code correction results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCorrectFilter
  implements OutputFilter<List<CodeCorrectResult>>
{
  public static final CodeCorrectFilter instance = new CodeCorrectFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<CodeCorrectResult> _result)
  {
    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for(CodeCorrectResult result : _result){
        // filter out corrections that have no preview, since they can't be
        // applied in the same fashion as those that have previews.
        if(result.getPreview() != null){
          if(buffer.length() == 0){
            buffer.append(result.getProblem().getMessage());
          }
          buffer.append('\n').append(result.getIndex())
            .append('.').append(result.getProblem().getSourceStart())
            .append(":  ").append(result.getDescription());

          String preview = result.getPreview()
              .replaceAll("<br>", "\n")
              .replaceAll("<.+>", "")
              .replaceAll("\\n\\s*", "\n\t");
          buffer.append("\n\t").append(preview.trim());
        }
      }
    }
    return buffer.toString();
  }
}
