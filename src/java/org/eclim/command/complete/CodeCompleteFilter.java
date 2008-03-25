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
package org.eclim.command.complete;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.command.OutputFilter;

/**
 * Filter for code completion results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCompleteFilter
  implements OutputFilter<List<CodeCompleteResult>>
{
  public static final CodeCompleteFilter instance = new CodeCompleteFilter();

  private static final String DELIMETER = "|";

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<CodeCompleteResult> _result)
  {
    String delimeter = null;
    try{
      delimeter = _commandLine.hasOption(Options.DELIMETER_OPTION) ?
        _commandLine.getValue(Options.DELIMETER_OPTION) : DELIMETER;
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    StringBuffer buffer = new StringBuffer();
    if(_result != null){
      for(CodeCompleteResult result : _result){
        if(buffer.length() > 0){
          buffer.append('\n');
        }

        buffer.append(result.getCompletion()).append(delimeter);

        if(result.getShortDescription() != null){
          buffer.append(result.getShortDescription());
        }

        buffer.append(delimeter);

        if(result.getDescription() != null){
          buffer.append(result.getDescription());
        }
      }
    }
    return buffer.toString();
  }
}
