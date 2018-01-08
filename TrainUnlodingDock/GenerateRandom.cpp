//
//  GenerateRandom.cpp
//  Train Unloading Dock
//
//  Created by Zhao Wenbo on 2017/10/10.
//  Copyright © 2017年 Zhao Wenbo. All rights reserved.
//

#include "GenerateRandom.hpp"
#include <time.h>
#include <stdlib.h>
#include <math.h>

float GenerateRandom:: genTNextarrivetime(float time){
    float temp=rand() / float(RAND_MAX);
    //printf ("temp:%f",temp);
    float next= -log(temp)*time;
    //printf("next:%f\n",next);
    return next;
}

float GenerateRandom::genfloat(float min, float max){
    float a = rand() / float(RAND_MAX);
    min++;
    float b=(float)((rand() % (int)(max- min + 1))+ min);
    b=b-1;
    return a+b;
}

float GenerateRandom::genRemainingworktime(){
    return genfloat(6.0, 11.0);
}
float GenerateRandom::genCNextarrivetime(){
    return genfloat(2.5, 3.5);
}
float GenerateRandom::genUnloadtime(){
    return genfloat(3.5, 4.5);
}
