/*
 * SSEJaccard.cpp
 * Copyright (c) 2016 Hamidreza Mohebbi All Rights Reserved.
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

#include "SSEJaccard.hpp"
#include <x86intrin.h>
#include <omp.h>
#include <sstream>
#include <emmintrin.h>
using namespace std;

template<class T>
inline uint8_t n(T val) {
  if (val > 0)
    return 1;
  return 0;
}

template<class T>
inline T max(T a1, T a2) {
  if (a1 > a2)
    return a1;
  return a2;
}

template<class T>
inline T min(T a1, T a2) {
  if (a1 < a2)
    return a1;
  return a2;
}

bool cmp(const std::pair<int, float> &p1, const std::pair<int, float> &p2) {
  return p1.second < p2.second;
}

// read a binary files and extract features
void SSEJaccard::readBinaryFeature(const char *fname, std::vector<__m512i> &data_vector,
std::vector<unsigned int> &ids, unsigned int &num_seq) {

    std::ifstream ifs;
    ifs.open(fname, ios::in | ios::binary);
    __m512i content;

  if (!ifs) {
    std::cerr << "can not open " << fname << std::endl;
    exit(0);
  }
    
  // get length of file:
  ifs.seekg (0, ifs.end);
  int filelen = ifs.tellg();
  ifs.seekg(0, ifs.beg);     

  maxitem = INT_MIN;
  int val = 0;
  int readbytes = 0;
  std::string line;
  std::stringstream strbuff;
  ifs.clear();  
  while ( readbytes < filelen ) { 
  //while( readbytes < 32 * 102400) {
    ifs.read((char*)&content, sizeof(__m512i));
    ifs.seekg(64, ios::cur); // we read in 64 bytes chunk
    readbytes = readbytes + 64;
    data_vector.resize(data_vector.size() + 1);    
    data_vector[data_vector.size() - 1] = content;
  
  }
  
  ids.resize(data_vector.size());
  for(int i=0; i<data_vector.size(); i++)
	ids[i] = i;
  num_seq = data_vector.size();
}

inline size_t SSEJaccard::popCnt(__m128i in){
  size_t popcnt;
  const __m128i mask_55 = _mm_set1_epi8(0x55);
  const __m128i mask_33 = _mm_set1_epi8(0x33);
  const __m128i mask_0F = _mm_set1_epi8(0x0F);
  __m128i v, tmp;
  v = in;
  tmp = _mm_srli_epi64(v, 1);
  tmp = _mm_and_si128(tmp, mask_55);
  v = _mm_sub_epi64(v, tmp);
  tmp = _mm_srli_epi64(v, 2);
  tmp = _mm_and_si128(tmp, mask_33);
  v = _mm_and_si128(v, mask_33);
  v = _mm_add_epi64(v, tmp);
  tmp = _mm_srli_epi64(v, 4);
  v = _mm_add_epi64(v, tmp);
  v = _mm_and_si128(v, mask_0F);
  v = _mm_sad_epu8(v, _mm_setzero_si128());
  popcnt = _mm_extract_epi16(v, 0) + _mm_extract_epi16(v, 4) + 0;
  return popcnt;
}

inline int SSEJaccard::popCnt256(const __m256i a){
	int popcnt = 0;
	
	int * p1 = (int *) &a;
	for(int i=0; i<8; i++){
		popcnt += _mm_popcnt_u32(p1[i]);
	}
	return popcnt;
}

inline int SSEJaccard::popCnt512(const __m512i a){
	int popcnt = 0;
	
	int * p1 = (int *) &a;
	for(int i=0; i<16; i++){
		popcnt += _mm_popcnt_u32(p1[i]);
	}
	return popcnt;
}

inline __m128i SSEJaccard::_mm_popcnt(const __m128i a)
{
        __m128i r;
        int * p1 = (int *) &a;
        int * p2 = (int *) &r;
        p2[0] = _mm_popcnt_u32(p1[0]);
        p2[1] = _mm_popcnt_u32(p1[1]);
        p2[2] = _mm_popcnt_u32(p1[2]);
        p2[3] = _mm_popcnt_u32(p1[3]);

        return r;
}


// this method calculates the actual jaccard distance using AVX instructions, the length of input is 256 bit for the chunkSize = 4
float SSEJaccard::calc256SSEJaccardDist(unsigned int id1, unsigned int id2){
	__m256i v, v1, v2, v3;
	int a,b;

	v = _mm256_load_si256((__m256i const*) &fp_data[id1]);
	v1 = _mm256_load_si256((__m256i const*) &chr_data[id2]);

	v2 = _mm256_castps_si256(_mm256_and_ps(_mm256_castsi256_ps(v), _mm256_castsi256_ps(v1)));
	v3 = _mm256_castps_si256(_mm256_or_ps(_mm256_castsi256_ps(v), _mm256_castsi256_ps(v1)));

	a = popCnt256(v2);
	b = popCnt256(v3);

	if(b == 0) return 0;
	return (1.f - float(a)/float(b));

}

float SSEJaccard::calc512SSEJaccardDist(unsigned int id1, unsigned int id2){
	__m512i v, v1, v2, v3;
	int a,b;

	v = _mm512_load_si512((__m512i const*) &fp_data[id1]);
	v1 = _mm512_load_si512((__m512i const*) &chr_data[id2]);

	v2 = _mm512_castps_si512 (_mm512_and_ps(_mm512_castsi512_ps(v), _mm512_castsi512_ps(v1)));
	v3 = _mm512_castps_si512 (_mm512_or_ps(_mm512_castsi512_ps(v), _mm512_castsi512_ps(v1)));

	a = popCnt512(v2);
	b = popCnt512(v3);

	if(b == 0) return 0;
	return (1.f - float(a)/float(b));

}

// writes the items in global variable result into file. 
void SSEJaccard::writeResult(params* param){
	for(int i=0; i< param->result.size(); i++){		
		(*param->os).write((char *)&param->result[i].id1, sizeof(unsigned int));
        (*param->os).write((char *)&param->result[i].id2, sizeof(unsigned int));
        (*param->os).write((char *)&param->result[i].dist, sizeof(float));
	}
}

// writes the items in global variable result into file, and remove written items. 
void SSEJaccard::writeRemoveResult(params* param){
	while(param->result.size() > 0){		
		(*param->os).write((char *)&param->result[0].id1, sizeof(unsigned int));
        (*param->os).write((char *)&param->result[0].id2, sizeof(unsigned int));
        (*param->os).write((char *)&param->result[0].dist, sizeof(float));
		param->result.erase(param->result.begin());
	}
}


void SSEJaccard::jaccardDist_kernel(int numthread, int sizeA, int sizeB,params *param){
	int i,j,k ,step,aBegin, bBegin, tid, my_first_A, my_last_A,my_first_B, my_last_B , nThreads;	
	
	// padding sizes
	int sizeA_P = ((sizeA + (BLOCK_SIZE - 1)) / BLOCK_SIZE) * BLOCK_SIZE;
	int sizeB_P = ((sizeB + (BLOCK_SIZE - 1)) / BLOCK_SIZE) * BLOCK_SIZE;
	
	for(aBegin=0; aBegin<sizeA_P; aBegin+=BLOCK_SIZE){
		for(bBegin=0; bBegin<sizeB_P; bBegin+= BLOCK_SIZE){
			#pragma omp parallel shared(param) private(i,j,k,step, nThreads,tid,my_first_A,my_last_A, my_first_B, my_last_B)
			{
				tid = omp_get_thread_num();
				nThreads = omp_get_num_threads();
				my_first_A =  aBegin + (   tid       * BLOCK_SIZE) / nThreads;
				my_last_A  =  aBegin + ( ( tid + 1 ) * BLOCK_SIZE ) / nThreads - 1;
				if(my_last_A > sizeA)
					my_last_A = sizeA - 1;
				my_first_B =  bBegin + (   tid       * BLOCK_SIZE) / nThreads;
				my_last_B  =  bBegin + ( ( tid + 1 ) * BLOCK_SIZE ) / nThreads - 1;
				if(my_last_B > sizeB)
					my_last_B = sizeB - 1;
				
				float jDist[4];
				float jdist = 0.0f;
				for(i= my_first_A; i<= my_last_A; i++){
					for(j= my_first_B; j< my_last_B; j++){ 
                                                jdist = calc512SSEJaccardDist(param->ids_fp[i], param->ids_chrfp[j]);                                              
                                     		//cout << "jdist: " << jdist << endl;           
                                                if(jdist <= param->jaccardDist && i != j ){
                                                        #pragma omp critical
                                                	param->result.push_back({param->ids_fp[i], param->ids_chrfp[j], jdist});
                                                }
                                                                                               
                                        }
					/*step = (my_last_B - my_first_B + 1) / 4;	
					for(j= 0; j< step; j++){ 
						calc4SSEJaccardDist(param->ids_fp[i], param->ids_chrfp[my_first_B + j * 4],jDist);						
						for(k=0; k<4; k++){
							if(jDist[k] <= param->jaccardDist){
								#pragma omp critical
								param->result.push_back({param->ids_fp[i], param->ids_chrfp[my_first_B + j * 4 + k], jDist[k]});
							}
						}						
					}
					for(j= (my_first_B + step  * 4); j<= my_last_B; j++){ 
						jdist = calc1JaccardDist(param->ids_fp[i], param->ids_chrfp[j]);					
						if(jdist <= param->jaccardDist){
							#pragma omp critical
							param->result.push_back({param->ids_fp[i], param->ids_chrfp[j], jdist});
						}											
					}*/
				}
			}
		}		
	}

}

void SSEJaccard::run(const char *fname, const char * chrname, const char *oname, int numthread, float _jaccardDist)
{    
  params * param = new params;  
  param->jaccardDist = _jaccardDist;
  
  std::ofstream ofs;
  ofs.open(oname, ios::out | ios::binary); //create the binary output file.
  param->os = &ofs;
  
  readBinaryFeature(fname, fp_data, param->ids_fp, param->num_seq_fp);
  cout << "input: " << fname << " #records: " << fp_data.size() << endl;   
  readBinaryFeature(chrname, chr_data, param->ids_chrfp, param->num_seq_chrfp);  
  cout << "reference: " << chrname << " #records: " << chr_data.size() << endl;   
  cout << "output: " << oname << endl;
  
  struct timeval tv1, tv2;
  gettimeofday(&tv1,NULL);
  omp_set_num_threads(numthread);
  jaccardDist_kernel(numthread, fp_data.size(), chr_data.size(), param);  
  //jaccardDist_kernel(numthread, 102400, 102400, param);
  writeResult(param);
  gettimeofday(&tv2,NULL);
  int sec, usec;
  sec = (int) (tv2.tv_sec-tv1.tv_sec);
  usec = (int) (tv2.tv_usec-tv1.tv_usec);
  if (usec < 0){
	sec--;
	usec += 1000000;
  }
	
  cout << "Jaccard Distance kernel cputime: " << sec+usec/1000000.0f << " seconds."<<endl;  
  cout << "Number of results: " << param->result.size() << endl; 
 
  ofs.close();

}
