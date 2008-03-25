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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

/**
 * Output filter for import results.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImportFilter
  implements OutputFilter<List<ImportResult>>
{
  public static final ImportFilter instance = new ImportFilter();

  /**
   * {@inheritDoc}
   */
  public String filter (CommandLine _commandLine, List<ImportResult> _result)
  {
    if(_result != null){
      ArrayList<String> elements = new ArrayList<String>(_result.size());
      for(ImportResult result : _result){
        elements.add(result.getElement());
      }
      Collections.sort(elements);
      return StringUtils.join(elements.toArray(), '\n');
    }
    return "";
  }
}
