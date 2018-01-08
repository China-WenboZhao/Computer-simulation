//
//  GenerateRandom.hpp
//  Train Unloading Dock
//
//  Created by Zhao Wenbo on 2017/10/10.
//  Copyright © 2017年 Zhao Wenbo. All rights reserved.
//

#ifndef GenerateRandom_hpp
#define GenerateRandom_hpp

#include <stdio.h>
class GenerateRandom{
public:
    
    //correspond to poisson process
    float genTNextarrivetime(float time);
    //random uniformly
    float genfloat(float min, float max);
    float genRemainingworktime();
    float genCNextarrivetime();
    float genUnloadtime();
};
#endif /* GenerateRandom_hpp */
