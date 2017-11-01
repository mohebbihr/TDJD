/*
 * Main.cpp
 * Copyright (c) 2011 Yasuo Tabei All Rights Reserved.
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

#include <iostream>
#include <cstdlib>

/* Globals */
void usage();
void version();
void parse_parameters (int argc, char **argv);

// infname is the file conatains the binary finger print of input file.
// inRefFP is the file conatains the binary finger print of chromosome file.
// outfname is the output filename.
char *infname, *inRefFP, *outfname;
int numthread = 24;
float jaccardDist  = 0.15; // jaccardDist = 1 - jaccard Index . for 0.95 similarity jaccardDist = 0.05

int main(int argc, char **argv) 
{
  version();

  parse_parameters(argc, argv);
  //ParallelSSEJaccard mainthread;
  //mainthread.run(infname, inRefFPDir, outDir, numthread, jaccardDist);
  std::cout << "Jaccard Distance start, with jaccard Distance Threashold: " << jaccardDist << " , using "<< numthread << " threads."<<std::endl;
  SSEJaccard * t = new SSEJaccard;
  t->run(infname, inRefFP,outfname, numthread, jaccardDist);
  delete t;
  return 0;
}

void version(){
  std::cerr << "SSEJaccard Distance version 1.0" << std::endl;
}

void usage(){
  std::cerr << std::endl
       << "Usage: ssejaccard [OPTION]... IN_FP_FILE IN_CHR_FILE OUT_FILE_PATH" << std::endl;       
  exit(0);
}

void parse_parameters (int argc, char **argv){
  
  int argno;
  for (argno = 1; argno < argc; argno++){
    if (argv[argno][0] == '-'){
      if      (!strcmp (argv[argno], "-version")){
	version();
      }
      else if (!strcmp (argv[argno], "-jaccard")) {
	if (argno == argc - 1) std::cerr << "Must specify miximum itemset size after -jaccard" << std::endl;
	jaccardDist = atof(argv[++argno]);
      }
      else if (!strcmp (argv[argno], "-numt")) {
        if (argno == argc - 1) std::cerr << "Must specify number of threads after -numt" << std::endl;
        numthread = atoi(argv[++argno]);
      }else {
	usage();
      }
    } else {
      break;
    }
  }
  if (argno + 1 >= argc)
    usage();

  infname = argv[argno];
  inRefFP = argv[argno + 1];
  outfname = argv[argno + 2];
}
