(ns clj-drone.core-test
  (:import (java.net DatagramPacket))
  (:use clojure.test
        midje.sweet
        clj-drone.core
        clj-drone.at))

(deftest core-tests
  (fact "default initialize gets default host and port"
    (.getHostName drone-host) => default-drone-ip
    at-port => default-at-port
    (against-background (before :facts (drone-initialize))))

  (fact "custom initiliaze uses custom host and port"
    (.getHostName drone-host) => "192.168.2.2"
    at-port => 4444
    (against-background (before :facts (drone-initialize "192.168.2.2" 4444))))

  (fact "drone command passes along the data to send-command"
    (drone :take-off) => anything
    (provided
      (send-command  "AT*REF=2,290718208\r") => 1)
    (against-background (before :facts (drone-initialize))))

  (fact "drone-do-for command calls drone command every 30 sec"
    (drone-do-for 1 :take-off) => anything
    (provided
      (drone :take-off nil nil nil nil) => 1 :times #(< 0 %1))
    (against-background (before :facts (drone-initialize)))))


;; (run-tests 'clj-drone.core-test)