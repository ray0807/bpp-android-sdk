/*
 *   Copyright (c) 2016 - 2017 Kulykov Oleh <info@resident.name>
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */


#include <stdlib.h>
#include <assert.h>
#include <string.h>

#if defined(CMAKE_BUILD)
#undef CMAKE_BUILD
#endif


#if defined(XCODE)
#include "libnhr.h"
#include "nhr_memory.h"
#include "nhr_string.h"
#include "nhr_gz.h"
#else
#include "../http/libnhr.h"

// include_directories(${CMAKE_CURRENT_SOURCE_DIR})
#include "../http/src/nhr_memory.h"
#include "../http/src/nhr_string.h"
#include "../http/src/nhr_gz.h"
#endif


#if defined(CMAKE_BUILD)
#undef CMAKE_BUILD
#endif

#include "cJSON/cJSON.h"

// test_libnhr_creation

NHR_API(int) test_create(void);


// test_gz_creation

NHR_API(int) test_gz_creation(void);


// test_get
NHR_API(int) test_get(void);


// test_post
NHR_API(int) test_post(void);
