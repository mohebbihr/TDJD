# TDJD
This is the github repository of the research paper entitled "Detecting Chromosomal Structural Variation using Jaccard Distance and Parallel Architecture" which is published at BIBM17 conference. If you used this source code, please cite this paper.

In this work, we proposed a method called TDJD that identifies the location of inter-chromosomal breakpoints corresponding to 
large scale structural variations, in particular translocations and insertions. To reduce the huge dimension of the search space, we split candidate reads that can be potential breakpoints into windows, and represent the windows as a sequence of binary fingerprints. We then search for the location of the breakpoint in the reference genome using Jaccard
distance. 

 We use a combination of parallel computing, search using Jaccard distance to solve the exact nearest neighbor problem. The
 dimensionality reduction takes advantage of an SSE multi-thread architecture to achieve efficient search.
 
 You can read more about this research using this link (https://ieeexplore.ieee.org/abstract/document/8217962/): 

You can find out the source code of this project in this repository. The TDJD.sh script is the main script and each folder contains the code for the pipeline, you need to compile the code in subdirectories first, then run the main script. 

Thanks

