" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Compiler for ant (enhancement to default ant compiler provided w/ vim).
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

if exists("current_compiler")
  finish
endif
let current_compiler = "eclim_ant"

if !exists('g:EclimAntCompilerAdditionalErrorFormat')
  let g:EclimAntCompilerAdditionalErrorFormat = ''
endif

if !exists('g:EclimAntErrorFormat')
  let g:EclimAntErrorFormat = '\%A%f:%l:\ %m,'
endif

CompilerSet makeprg=ant\ -find\ build.xml\ $*

" The two entries before the last one, are for catching ant build file names
" and error line numbers.
exec 'CompilerSet errorformat=' .
  \ '\%-G%.%#[javac]\ %.%#:\ warning:\ unmappable\ character\ %.%#,' .
  \ '\%A%.%#[javac]\ %f:%l:\ %m,' .
  \ '\%C%.%#[javac]\ symbol\ %#:\ %m,' .
  \ '\%-Z%.%#[javac]\ %p^,' .
  \ '\%A%.%#[javadoc]\ %f:%l:\ %m,' .
  \ '\%-C%.%#[javadoc]\ location:\ %.%#,' .
  \ '\%-C%.%#[javadoc]\ %#,' .
  \ '\%-Z%.%#[javadoc]\ %p^,' .
  \ '\%-G%.%#[javadoc]\ Note:%.%#,' .
  \ '\%-G%.%#[javadoc]\ javadoc:%.%#,' .
  \ '\%.%#[javadoc]\ %f:\ %m,' .
  \ '\%.%#[java]\ org\.apache\.jasper\.JasperException:\ file:%f(%l\\,%c)\ %m,' .
  \ '\%+A%.%#[junit]\ %.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[junit]\ %.%#Errors:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[junit]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[cactus]\ %.%#Failures:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[cactus]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#[cactus]\ %.%#Errors:\ %[%^0]%.%#\ Time\ elapsed:\ %.%#,' .
  \ '\%-Z%.%#[cactus]\ Test\ %f\ FAILED,' .
  \ '\%+A%.%#eclim\ testng:\ %f:%m,' .
  \ '\%A%.%#\ ERROR\ %.%#\ line\ %l\ in\ file:\ %.%f%.:\ %m,' .
  \ g:EclimAntCompilerAdditionalErrorFormat .
  \ '\%A%.%#[exec]\ %f:%l:%c:\ %m,' .
  \ '\%A%.%#[exec]\ %f:%l:\ %m,' .
  \ '\%A%f:%l:%c:\ %m,' .
  \ g:EclimAntErrorFormat .
  \ '\%-G%.%#'

" vim:ft=vim:fdm=marker
