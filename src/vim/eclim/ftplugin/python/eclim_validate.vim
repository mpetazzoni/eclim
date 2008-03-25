" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/validate.html
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
"
" This program is free software: you can redistribute it and/or modify
" it under the terms of the GNU General Public License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" This program is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General Public License for more details.
"
" You should have received a copy of the GNU General Public License
" along with this program.  If not, see <http://www.gnu.org/licenses/>.
"
" }}}

" Global Variables {{{
if !exists("g:EclimPythonValidate")
  let g:EclimPythonValidate = 1
endif
" }}}

if g:EclimPythonValidate
  augroup eclim_python_validate
    autocmd!
    autocmd BufWritePost *.py call eclim#python#validate#Validate(1)
  augroup END
endif

" Command Declarations {{{
if !exists(":Validate")
  command -nargs=0 -buffer Validate :call eclim#python#validate#Validate(0)
endif
if !exists(":PyLint")
  command -nargs=0 -buffer PyLint :call eclim#python#validate#PyLint()
endif
" }}}

" vim:ft=vim:fdm=marker
