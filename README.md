# clj-drone

Why wouldn't you want to control your AR Drone with your Clojure REPL?

A Clojure library to control the Parrot AR Drone 2.0 [http://ardrone2.parrot.com/](http://ardrone2.parrot.com/)

## Usage

Just beginning the start of the control library
More to come soon.

If you are interested in seeing some demo videos, check out the
[blog post](http://gigasquidsoftware.com/wordpress/?p=645).

Using with Leiningen
```clojure
(defproject your-project "0.1.0-SNAPSHOT"
  :description "Your project"
  :dependencies [[clj-drone "0.1.2"]])
````

Sample Usage - Drone Takes off for 10 seconds and then lands

```clojure
(ns clj-drone.example
  (:use clj-drone.core))

(drone-initialize)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
```

## Movement Commands Supported

```clojure
(drone :take-off)
(drone :land)
(drone :emergency) ;; restores control of drone after emergency landing
(drone :hover)
(drone :flat-trim) ;; called on initialize as well to tell drone it is flat on the ground
```

The movement commands take argument from 0-1

```clojure
(drone :spin-right 0.5)
(drone :spin-left 1)
(drone :up 0.5)
(drone :down 1)
(drone :tilt-back 0.3)
(drone :tilt-front 1)
(drone :tilt-right 1)
(drone :tilt-left 1)
```

The fly command takes arguments left-right tilt, front-back tilt,
vertical speed, angular speed.  The arguments are all in the range -1
to 1.

```clojure
(drone :fly 0.3 -0.9 0.7 -0.2)
```

The drone-do-for command does a command for x second

```clojure
(drone-do-for 4 :take-off) ;=> take off for 4 seconds
(drone-do-for 2 :spin-right 0.8) ;=> spin right at 80% for 2 seconds
(drone-do-for 2 :spin-left 0.3) ;=> spin left at 30% for 2 seconds
```
The drone reset-watchdog command is used for when the drone command sequence
number gets out of sync.  It will ignore commands that come in with a bad or out of sync sequence number.  Calling this resets it to zero.  The drone will drop it's connection and reset its counter to 0 by itself if no command is received from the client for 2 seconds.
````clojure
(drone :reset-watchdog)
````

Visual tag targeting commands

````clojure
(drone :init-targeting) ;=> sets up drone to receive navigation visual tags
(drone :target-shell-h) ;=> detects indoor and outdoor hull tags on horizontal camera
(drone :target-roundel-v) ;=> detects the roundel tag on the vertical camera
(drone :target-color-green) ;=> detects the green indoor/outhull hull tags
(drone :target-color-yellow) ;=> detects the yellow indoor/outhull hull tags
(drone :target-color-blue) ;=> detects the blue indoor/outhull hull
tags
(drone :hover-on-roundel) ;=> puts the drone in a mode where it will follow the roundel on the ground
(drone :free-flight) ;=> puts the drone back in normal mode accepting movement commands
````

Animations or Tricks
`````clojure
(drone :anim-yaw-shake) ;=> really fast back and forth yaw turn
(drone :anim-turnaround) ;=> kinda a jerky turnaround
(drone :anim-wave) ;=> my favorite - kinda like a hoola-hoop
(drone :anim-double-phi-theta-mixed) ;=> kinda crazy move had to describe
(drone :anim-flip-right) ;=> Classic flip goodness
````

## Navigation Data
To start receiving the navigation data, you must issue the command.
````clojure
(drone-init-navdata)
```
This will start a log in logs/drone.log that will record the
navigation data for you.  You can control what is printed out to the
log with:
```clojure
(set-log-data [nav-data-keys])
```
Example:
````clojure
(set-log-data [:seq-num :flying :battery-percent :control-state :roll :pitch :yaw
                :velocity-x :velocity-y :velocity-z])
```
The Navigation Data is stored in a atom that is a map.  You can
de-reference with:
````clojure
@nav-data
```
The possible values are:
````clojure
;;General State Info

:flying ;=> :landed or :flying
:video  ;=> :off or :on
:vision ;=> :off or :on
:control ;=> :euler-angles or :angular-speed
:altitude-control ;=> :off or :on
:user-feedback ;=> :off or :on
:command-ack ;=> :none or :received
:camera ;=> :not-ready or :ready
:travelling ;=> :off or :on
:usb ;=> :not-ready or :ready
:demo ;=> :off or :on
:bootstrap ;=> :off or :on
:motors ;=> :ok or :motor-problem
:communication ;=> :ok or :communication-lost
:software ;=> :ok or :sofware-fault
:battery ;=> :ok or :too-low
:emergency-landing ;=> :off or :on
:timer ;=> :not-elapsed or :elapsed
:magneto ;=> :ok or :needs-calibration
:angles ;=> :ok or :out-of-range
:wind ;=> :ok or :too-much
:ultrasound ;=> :ok or :deaf
:cutout ;=> :ok or :detected
:pic-version ;=> :bad-version or :ok
:atcodec-thread ;=> :off or :on
:navdata-thread ;=> :off or :on
:video-thread ;=> :off or :on
:acquistion-thread ;=> :off or :on
:ctrl-watchdog ;=> :ok or :delay
:adc-watchdog ;=> :ok or :delay
:com-watchdog ;=> :ok or :problem
:emergency ;=> :ok or :detected

;;Demo Options
:control-state ;=> possible values [:default :init :flying :hovering :test :trans-takeoff :trans-gotofix :trans-landing :trans-looping]
:battery-percent ;=> value between 0 - 1
:pitch ;=> in degrees
:roll ;=> in degrees
:yaw ;=> in degrees
:altitude ;=> in meters
:velocity-x
:velocity-y
:velocity-z

;;Visual Target/Tag Detection
:targets-num  ;=> number of tags detected 0 - 4
:detect-camera-type ;=> :non-disabled or :multiple (meaning horizonal and
vertical)
:targets ;=>  [vec of targets detected if targets-num > 0 ]
     [{:target-type :horizontal or :vertical or :vertical-hsync
       :target-xc ;=> in cm
       :target-yc ;=> in cm
       :target-width ;=> in cm
       :target-height ;=> in cm
       :target-dist ;=> in cm
       :target-orient-angle ;=> in degrees if the target is roundel,
       :target-camera-source ;=>(same as target-type - as far as I can tell anyway)}]
```

The nav stream will keep going unless interrupted or called to end by
giving the command
````clojure
(end-navstream)
```
Example Program to log navigation data for a flight
````clojure
(ns clj-drone.example.nav-test
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;; logging is configured to go to the logs/drone.log file

(set-log-data [:seq-num :flying :battery-percent :control-state :roll :pitch :yaw
                :velocity-x :velocity-y :velocity-z])
(drone-initialize)
(drone-init-navdata)
(drone :take-off)
(Thread/sleep 10000)
(drone :land)
(end-navstream)
````

## Detecting Targets with the Navigation Data
You can configure the drone to detect pre-defined tags
on the horizontal and vertical cameras. You first need to tell the
drone which targets to detect on what cameras.  Here is an example
program:
````clojure
(ns clj-drone.example.target-test
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;; logging is configured to go to the logs/drone.log file

(set-log-data [:seq-num :battery-percent :control-state :detect-camera-type
               :targets-num :targets])
(drone-initialize)
(drone :init-targeting)
(drone :target-shell-h)
(drone :target-color-blue)
;; the drone will look for targets with blue tags on the horizontal camera
(drone :target-roundel-v)
;; the drone will look for the black and white roundel on the vertical camera
(drone-init-navdata)
;; watch the drone.log file and move the drone above the roundel
;; target and put the hull in front of the horizontal camera

(end-navstream)  ;; this ends the logging

```

## Auto Navigation with Goals and Beliefs
Why shouldn't the AR drone have goals and beliefs?
Inspired by John McCarthy's
[Ascribing Mental Qualities to Machines](http://www-formal.stanford.edu/jmc/ascribing/ascribing.html)
, the AR drone can execute behavoir based on defined goals and beliefs
and log its progress in the drone.log.  The way it works is the
function that continually processes the navigation input, looks at the
atom that holds the list of the current goals.  Currently, the drone
processes these goals one at a time.  That is it will not process the
second goal in the list until the first goal completes.

### Defining belief-actions
Belief-actions are defined using the def-belief-action macro.  It
takes a readable str for the belief, a predicate function of the
nav-data map to see if it "believes" the statement or not.  Finally,
it takes another function of the nav-data map to define what the
action it should take is, if it holds the belief to be true. Example:
````clojure
(def-belief-action ba-landed
  "I am landed"
  (fn [{:keys [control-state]}] (= control-state :landed))
  (fn [navdata] (drone :take-off)))
````

### Defining belief-goals
Goals are defined using the def-goal macro. It takes a readable str
for the goal, a predicate function of the nav-data map to see if the
goal has been reached.  Finally it takes a vector of belief actions
that it evaluates until the goal has been reached. Example:
````clojure
(def-goal g-take-off
  "I want to fly."
  (fn [{:keys [control-state]}] (= control-state :hovering))
  [ba-landed ba-taking-off])
```

### Setting the current goals
The list of current goals is set via function set-current-goal-list.
The current-goal-list is an atom, so the goals can be changed mid
flight and immediately communicated to the navigation thread. Example:
````clojure
(set-current-goal-list [g-take-off g-cruising-altitude g-land])
```

For a further example of navigation goal processing, see [example/nav-goals](https://github.com/gigasquid/clj-drone/blob/master/examples/nav_goals.clj)


## Running tests
The tests use [Midje](https://github.com/marick/Midje).  Use the midje
[lein plugin](https://github.com/marick/Midje/wiki/Lein-midje)

    lein midje


## To do list
- incoming navigation stream angles
- incoming video stream

## License

Copyright © 2013 Carin Meier

Distributed under the Eclipse Public License, the same as Clojure.
