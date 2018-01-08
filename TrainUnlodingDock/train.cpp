//
//  train.cpp
//  Train Unloading Dock
//
//  Created by Zhao Wenbo on 2017/10/10.
//  Copyright © 2017年 Zhao Wenbo. All rights reserved.
//

#include "train.hpp"
void train::set(int index, float arrive_time,float unload_last, float remaining_work_time, float crew_arrive_time){
    Index=index;
    Arrive_time= arrive_time;
    Unload_last= unload_last;
    Remaining_work_time=remaining_work_time;
    Crew_arrive_time= crew_arrive_time;
}
void train::setCrew(float remaining_work_time,float crew_arrive_time){
    Remaining_work_time=remaining_work_time;
    Crew_arrive_time=crew_arrive_time;
}
void train::setHogged_start_time(float t){
    Hogged_start_time = t + Remaining_work_time;
}
void train::setHogged_end_time(){
    Hogged_end_time= Hogged_start_time + Crew_arrive_time;
}
void train::setEnter_dock_time(float enter_dock_time){
    Enter_dock_time=enter_dock_time;
}
void train::setDepature_time(float departure_time){
    Departure_time=departure_time;
}
void train::Hogged_num_add(){
    Hogged_out_num ++;
}
int train::getIndex(){
    return Index;
}
int train::getHoggedoutnum(){
     return Hogged_out_num;
}
float train::getArrivetime(){
     return Arrive_time;
}
float train::getHoggedstarttime(){
     return Hogged_start_time;
}
float train::getHoggedendtime(){
    return Hogged_end_time;
}
float train::getCrewarrivetime(){
     return Crew_arrive_time;
}
float train::getEnterdocktime(){
     return Enter_dock_time;
}
float train::getDeparturetime(){
     return Departure_time;
}
float train::getUnloadlast(){
    return Unload_last;
}
