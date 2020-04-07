# pn-draw
quil-based drawing and manipulation of Petri nets

![alt text](https://raw.githubusercontent.com/pdenno/pn-draw/master/data/screenshots/shot1.jpg "Humble start.")

## Status

This program draws Petri nets (PNs); one is depicted above. 
I have started work to allow stepping through the states of Generalized Stochastic Petri Nets (GSPNs). 
That code needs a little more work. See simulate.clj. 

## Installing and running the program

`lein repl`

`(in-ns 'pdenno.pn-draw.core)`

`(fig :fig-5)`

For development of the code, you can then connect to the started repl (e.g. with `cider-connect`). 

Check out the resources/public/PNs/jms directory for example of how to code a PN as .edn. 

Currently, you can run GSPNs (PNs with non-immediate transitions) manually using `(handle-sim-step!)`. 

## To Do

* Finish the interactive stuff in simulate.clj
* Fix the problem with twitchy arc placement. 
* Allow stepping through untimed PNs. 

