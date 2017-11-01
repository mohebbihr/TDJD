/*
 * SSEJaccard.hpp
 * Copyright (c) 2016 Hamidreza Mohebbi.
 * Email: mohebbi.h@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE and * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

#ifndef SSEJACCARD_HPP
#define SSEJACCARD_HPP

#include <iostream>
#include <algorithm>
#include <string>
#include <vector>
#include <list>
#include <map>
#include <cmath>
#include <cstring>
#include <iterator>
#include <fstream>
#include <strstream>
#include <iostream>
#include <sstream>
#include <cstdio>
#include <cstdlib>
#include <set>
#include <stdint.h>
#include <time.h>
#include <sys/time.h>
#include <limits.h>
#include <x86intrin.h>
#include <emmintrin.h>

#define BLOCK_SIZE 1024

// struct for stroing results.
struct outData {
	unsigned int id1;
	unsigned int id2;
	float dist;
};

struct outfiles {
  std::vector<std::ofstream *> os;
  std::vector<std::string> indirs;
  std::vector<std::string> outdirs;
};

struct params {

  unsigned int num_seq_fp; // number of sequences for fp input
  unsigned int num_seq_chrfp; // number of sequences for fp chromosome input
  float         jaccardDist;
  std::vector<unsigned int> ids_fp; // ids for fp input
  std::vector<unsigned int> ids_chrfp; // ids for fp chromosome input
  std::vector<outData> result; // a list of results.
  std::ofstream *os;
};

class SSEJaccard {

    std::vector<__m512i> fp_data;
    std::vector<__m512i> chr_data;  
  std::vector<float> norms;
  int                maxitem;
  uint32_t           num_char;
 
  void readBinaryFeature(const char *fname, std::vector<__m512i> &data_vector ,
std::vector<unsigned int> &ids, unsigned int &num_seq);    
  int popCnt256(__m256i in);
  int popCnt512(__m512i in);  
  float calc256SSEJaccardDist(unsigned int id1, unsigned int id2);
  float calc512SSEJaccardDist(unsigned int id1, unsigned int id2);  
  size_t popCnt(__m128i in);
  __m128i _mm_popcnt(const __m128i a);
  void writeResult(params* param);
  void writeRemoveResult(params* param);
  void jaccardDist_kernel(int numthread, int sizeA, int sizeB, params* param);  
 public:
  SSEJaccard() {};
  void run(const char *fname, const char * chrname, const char *oname, int numthread, float _jaccardDist);
};

class ParallelSSEJaccard {
	int getdir (const char * dir, std::vector<std::string> &files);
	double timeit(struct timeval tv1, struct timeval tv2);
	public:
		ParallelSSEJaccard(){};
		void run(const char *infname, const char * inRefFPDir, const char *outDir, int numthread, float jaccardDist);
};

#endif // SSEJACCARD_HPP
