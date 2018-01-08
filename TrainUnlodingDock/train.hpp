//
//  train.hpp
//  Train Unloading Dock
//
//  Created by Zhao Wenbo on 2017/10/10.
//  Copyright © 2017年 Zhao Wenbo. All rights reserved.
//

#ifndef train_hpp
#define train_hpp

#include <stdio.h>
enum State{
    NOTARRIVE, WAITING, HOGGEDOUT,UNLOADING, LEAVED
};

class train{
private:
    int Index; //the index of arriving trains
    int Hogged_out_num=0; // how many times has this train hogged out
    
    float Unload_last;
    float Remaining_work_time; //as there is only two variables related to crew, so to simplify we don't create Crew.class
    float Crew_arrive_time;
    
    /*time will use for events: arrive, hogged out, new crew arrive(hogged out end), enter dock, departure*/
    float Arrive_time=-1;
    float Hogged_start_time=-1;// equals to the arrive_time plus remaining work time
    float Hogged_end_time=-1;
    float Enter_dock_time=-1;
    float Departure_time=-1;

public:
    State state = NOTARRIVE;
    void set(int index, float arrive_time,float unload_last, float remaining_work_time, float crew_arrive_time);
    void setCrew(float remaining_work_time,float crew_arrive_time);
    void setHogged_start_time(float t);
    void setHogged_end_time();
    void setEnter_dock_time(float enter_dock_time);
    void setDepature_time(float departure_time);
    void Hogged_num_add();
    int getIndex();
    int getHoggedoutnum();
    float getUnloadlast();
    float getArrivetime();
    float getHoggedstarttime();
    float getHoggedendtime();
    float getCrewarrivetime();
    float getEnterdocktime();
    float getDeparturetime();
};
#endif /* train_hpp */
