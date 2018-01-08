//
//  main.cpp
//  Train Unloading Dock
//
//  Created by Zhao Wenbo on 2017/10/10.
//  Copyright © 2017年 Zhao Wenbo. All rights reserved.
//

#include <iostream>
#include <deque>
#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <math.h>
#include "train.hpp"
#include "GenerateRandom.hpp"

using namespace std;
float average_train_arrive=0.0;
float total_time =0.0;

deque<train> waitlist;
int countnum=0; //trains have arrived,start from 0
int train_index[5]={-1,-1,-1,-1,-1}; // the index of the train of the following event
float current_time = 0;
float event_list[5] ={-1,-1,-1,-1,-1};//the following event time
bool event_has_passed[5]={false,false,false,false,false}; // a mark that represents if the event in the event_list has happended or not
int event_type=-1;
GenerateRandom gr;
int continueunload=0;//a mark that will continue unload after hogged out
int hogged_out_trains[10]={0,0,0,0,0,0,0,0,0,0};

/*last required output*/
int serverd_trains=0;
float max_in_system_time=0;
float total_in_system_time=0.0;
float total_dock_busy_time= 0.0;
float total_dock_idle_time = 0.0;
float total_dock_hogged_out_time=0.0;
float area =0; // used to calculate average num of trains in queue;
int max_num_train_in_queue =0;

void init(){
    train t;
    t.set(countnum, 0 + gr.genTNextarrivetime(average_train_arrive), gr.genUnloadtime(),gr.genRemainingworktime(), gr.genCNextarrivetime());
    t.setEnter_dock_time(t.getArrivetime());
    t.state= NOTARRIVE;
    waitlist.push_back(t);
    event_list[0]=t.getArrivetime();
    event_list[3]=t.getEnterdocktime();
    train_index[0]=countnum;
    train_index[3]=countnum;
}
/*select the min hogged start time from the waitlist*/
float select_min_hogged_start_time(){
    float temp_time = 999999;
    train_index[1]=-1;
    for(int i=0;i<waitlist.size();i++){
        /*to avoid to get in a endless loop of the same value and select the min value, we use these four limitations */
        if(waitlist[i].getHoggedstarttime()>0 & waitlist[i].getHoggedstarttime()<=temp_time & waitlist[i].getHoggedstarttime()>=current_time
           & waitlist[i].state!=HOGGEDOUT){
            temp_time=waitlist[i].getHoggedstarttime();
            train_index[1] = waitlist[i].getIndex();
        }
    }
    return temp_time;
}
/*select the min hogged end time(min crew arrive time) from the waitlist*/
float select_min_crew_arrive_time(){
    float temp_time = 999999;
    train_index[2]=-1;
    for(int i=0;i<waitlist.size();i++){
        if(waitlist[i].getHoggedendtime()>0 &waitlist[i].getHoggedendtime()<=temp_time & waitlist[i].getHoggedendtime()>=current_time
           & waitlist[i].state==HOGGEDOUT){
            temp_time=waitlist[i].getHoggedendtime();
            train_index[2] = waitlist[i].getIndex();
        }
    }
    return temp_time;
}
/*find the position of the train in the waitlist by inputing the train number(index)*/
int findtrainbyIndex(int index){
    int a=-1;
    for(int i=0;i<waitlist.size();i++){
        if(index==waitlist[i].getIndex()){
            a=i;
        }
    }
    return a;
}
/*decide on the next event happens time and event times under the help of event_list*/
void timing(){
    event_list[1] = select_min_hogged_start_time();
    event_list[2] = select_min_crew_arrive_time();
    float temp_time=event_list[0];
    event_type = 0;
    /*choose the next event*/
    for(int i=1;i<5;i++){
        if(event_list[i]>0 & event_list[i] >= current_time & event_list[i] < temp_time & event_has_passed[i]==false){
            temp_time = event_list[i];
            event_type= i;
        }
    }
    current_time= temp_time;
}

/*
 * no matter what event happens, we must change the train state at once
 */

/*when one train arrives, we need to calculate init the next train and put it in the waitlist(queue)*/
void arrive(){
    event_has_passed[0]=true;
    if(findtrainbyIndex(train_index[0])!=-1){
        waitlist.back().setHogged_start_time(event_list[0]);
        waitlist.back().state= WAITING;
        printf("train %d arrived at %f.\n",train_index[0],event_list[0]);
    }else{
       // exit(0);
    }
    /*generate next arrival train*/
    float temp_time =gr.genTNextarrivetime(average_train_arrive);
    countnum++;
    train t;
    t.set(countnum, temp_time +event_list[0], gr.genUnloadtime(),gr.genRemainingworktime(), gr.genCNextarrivetime());
    t.state= NOTARRIVE;
    waitlist.push_back(t);
    event_list[0]=t.getArrivetime();
    event_has_passed[0]=false;
    train_index[0]=countnum;
    
    
}
/* when one train hog out, we need to set the new crew arrive time and if the train was unloading before hog out , we need to set a new departure time*/
void hog_out(){
    if(findtrainbyIndex(train_index[1])!=-1){
        if(waitlist[findtrainbyIndex(train_index[1])].state==UNLOADING ){
            waitlist[findtrainbyIndex(train_index[1])].setDepature_time(waitlist[findtrainbyIndex(train_index[1])].getDeparturetime()+waitlist[findtrainbyIndex(train_index[1])].getCrewarrivetime());
            event_list[4]=waitlist[findtrainbyIndex(train_index[1])].getDeparturetime();
            event_has_passed[4]=false;
            train_index[4]=train_index[1];
            continueunload=1; //a mark that will continue unload after hogged out
            
            total_dock_hogged_out_time = total_dock_hogged_out_time + waitlist[findtrainbyIndex(train_index[1])].getCrewarrivetime();
        }
        waitlist[findtrainbyIndex(train_index[1])].setHogged_end_time();
        waitlist[findtrainbyIndex(train_index[1])].state = HOGGEDOUT;
        printf("train %d hogged out at %f.\n",train_index[1],event_list[1]);
        
        waitlist[findtrainbyIndex(train_index[1])].Hogged_num_add();
    }else{
       // exit(0);
    }
}
/*when a new crew arrive, we need to set up the next hog out time of this train*/
void crew_arrive(){
    waitlist[findtrainbyIndex(train_index[2])].setCrew(12,gr.genCNextarrivetime());
    waitlist[findtrainbyIndex(train_index[2])].setHogged_start_time(event_list[2]);
    if(continueunload==1 & train_index[2]==train_index[3]){
        waitlist[findtrainbyIndex(train_index[2])].state=UNLOADING;
        continueunload=0;
    }else{
         waitlist[findtrainbyIndex(train_index[2])].state=WAITING;
    }
    printf("train %d new crews arrive at %f.\n",train_index[2],event_list[2]);
}
/* when a train enters dock, we will calculate and set the departure time first, but if the train hog out when unloading, the departure time will calculate again*/
void enter_dock(){
    event_has_passed[3]=true;
    waitlist[findtrainbyIndex(train_index[3])].setDepature_time(event_list[3]+waitlist[findtrainbyIndex(train_index[3])].getUnloadlast());
    event_has_passed[4]=false;
    train_index[4]=train_index[3];
    waitlist[findtrainbyIndex(train_index[3])].state=UNLOADING;
    event_list[4]=waitlist[findtrainbyIndex(train_index[3])].getDeparturetime();
    printf("train %d enter dock at %f.\n",train_index[3],event_list[3]);
    
    total_dock_busy_time =total_dock_busy_time+waitlist[findtrainbyIndex(train_index[3])].getUnloadlast();
}
/* when a train depart, we will first pop the train and then set the next train's enter_dock time according to the state*/
void depart(){
    event_has_passed[4]=true;
   
    switch (waitlist[findtrainbyIndex(train_index[4])].getHoggedoutnum()) {
        case 0:
            hogged_out_trains[0]++;
            break;
        case 1:
            hogged_out_trains[1]++;
            break;
        case 2:
            hogged_out_trains[2]++;
            break;
        case 3:
            hogged_out_trains[3]++;
            break;
        case 4:
            hogged_out_trains[4]++;
            break;
        case 5:
            hogged_out_trains[5]++;
            break;
        case 6:
            hogged_out_trains[6]++;
            break;
        case 7:
            hogged_out_trains[7]++;
            break;
        case 8:
            hogged_out_trains[7]++;
            break;
        case 9:
            hogged_out_trains[7]++;
            break;
        default:
            break;
    }
    
    waitlist.pop_front();
    if(waitlist.front().state==HOGGEDOUT){
        waitlist.front().setEnter_dock_time(waitlist.front().getHoggedendtime());
        
        total_dock_idle_time = total_dock_idle_time+waitlist.front().getHoggedendtime()- current_time;
    }else if(waitlist.front().state==NOTARRIVE){
        waitlist.front().setEnter_dock_time(waitlist.front().getArrivetime());
        
        total_dock_idle_time = total_dock_idle_time+waitlist.front().getArrivetime()- current_time;
    }else{
          waitlist.front().setEnter_dock_time(event_list[4]);
    }
    event_has_passed[3]=false;
    event_list[3]=waitlist.front().getEnterdocktime();
    train_index[3]=waitlist.front().getIndex();
    printf("train %d depart at %f.\n",train_index[4],event_list[4]);
    
    serverd_trains++;
    float temp_time=waitlist[findtrainbyIndex(train_index[4])].getDeparturetime()-waitlist[findtrainbyIndex(train_index[4])].getArrivetime();
    total_in_system_time = total_in_system_time + temp_time;
    if(temp_time>=max_in_system_time){
        max_in_system_time=temp_time;
    }
}

int main(int argc, const char * argv[]) {
    average_train_arrive = atoi(argv[1]);
    total_time = atoi(argv[2]);
    srand((unsigned)time(NULL));
    init();
    while(waitlist[0].getArrivetime()<=total_time){
        float last_current_time= current_time;
        timing();
        area= area+ (current_time - last_current_time)*waitlist.size();
        switch(event_type){
            case 0:
                arrive();
                break;
            case 1:
                hog_out();
                break;
            case 2:
                crew_arrive();
                break;
            case 3:
                enter_dock();
                break;
            case 4:
                depart();
                break;
        }
        if(waitlist.size()> max_num_train_in_queue){
            max_num_train_in_queue= (int)waitlist.size();
        }
        
    }
    printf("total number of train served:%d\n",serverd_trains-1);
    printf("everage time-in-system over trains:%f\n",total_in_system_time/serverd_trains);
    printf("max time-in-system over trains:%f\n",max_in_system_time);
    printf("dock busy percentage: %f\n",total_dock_busy_time/total_time);
    printf("dock idle percentage:%f\n",total_dock_idle_time/total_time);
    printf("dock hogged out percentage:%f\n",total_dock_hogged_out_time/total_time);
    printf("everage number of trains in queue: %f\n",area/total_time);
    printf("max number of trains in queue:%d\n",max_num_train_in_queue);
    for(int i=0;i<10;i++){
        printf("number of trains hodded out %d times:%d\n",i,hogged_out_trains[i]);
    }
    return 0;
}
