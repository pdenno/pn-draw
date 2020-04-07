# pn-draw
quil-based drawing and manipulation of Petri nets

![alt text](https://raw.githubusercontent.com/pdenno/pn-draw/master/data/screenshots/shot1.jpg "Humble start.")

## Status

This program draws Petri nets (PNs) as depicted above. I have started work to allow a user to advance her PN, 
following the token game, but it appears that I'm not done with that code. See simulate.clj. 

## Installing and running the program

`lein repl`

`(in-ns 'pdenno.pn-draw.core)`

`(fig :fig-5)`

You can then connect to this repl (e.g. with `cider-connect`) if you'd like.

Check out the resources/public/PNs/jms directory for example of how to code 

## To Do

* Finish the interactive stuff in simulate.clj
* Fix the problem with twitchy arc placement. 


