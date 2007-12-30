/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import org.eclim.command.taglist.TaglistScript;
import org.eclim.command.taglist.TagResult;

/**
 * Processes tags for java property files.
 */
class PropertiesTags implements TaglistScript
{
  public TagResult[] execute (String file)
  {
    def results = [];
    def lineNumber = 0;
    new File(file).eachLine {
      line -> processTag(line, ++lineNumber, file, results)
    };

    return (TagResult[])results.toArray(new TagResult[results.size()]);
  }

  void processTag (line, lineNumber, file, results)
  {
    def matcher = line =~ /^\s*([^#]+)\s*=.*/;
    if(matcher.matches()){
      def name = matcher[0][1];
      def tag = new TagResult(
        file:file, pattern:line, line:lineNumber, kind:'p', name:name);

      results.add(tag);
    }
  }
}
