(ns clj-drone.goals-test
  (:use clojure.test
        midje.sweet
        clj-drone.goals))

(defn belief-fn1 [] (= 1 1))
(defn belief-fn2 [] (= 1 2))
(defn action-fn1 [] (str "My first action fn"))
(def-belief-action ba1 "belief 1" belief-fn1 action-fn1)
(def-belief-action ba2 "belief 2" belief-fn2 action-fn1)
(defn goal-fn1 [] (= 2 2))
(defn goal-fn2 [] (= 2 3))
(def-goal g1 "goal 1" goal-fn1 [ba1 ba2])
(def-goal g2 "goal 2" goal-fn2 [ba1 ba2])

(defn reset-beliefs-goals []
  (reset! current-belief "None")
  (reset! current-goal "None"))

(fact "about def-belief-action"
  (let [ belief-str "I am on the ground"
         belief-fn #(+ 1 1)
         action-fn #(* 2 1)]
    (def-belief-action b-ground belief-str belief-fn action-fn) => anything
    (b-ground :belief-str "I am on the ground")
    ((b-ground :belief-fn)) => 2
    ((b-ground :action-fn)) => 2))

(fact "about def-goal"
  (let [ goal-str "Be Happy"
         goal-fn #(str ":)")
         smile (def-belief-action ba-sad "sad" #(str "Sad")  #(str "Smile"))]
    (def-goal be-happy goal-str goal-fn [smile]) => anything
    ((be-happy :goal-fn)) => ":)"
    (first (be-happy :belief-actions)) => smile))

(fact "eval-belief-action will execute the action if the belief-fn is true"
  (eval-belief-action ba1) => "My first action fn"
  @current-belief => "belief 1")

(fact "eval-belief-action will not execute action if the belief-fn is false"
  (eval-belief-action ba2) => nil
  @current-belief => "None"
  (against-background (before :facts (reset-beliefs-goals))))

(fact "about eval-goal when the goal has been reached"
  (eval-goal g1) => :goal-reached
  @current-belief => "None"
  @current-goal => "goal 1"
  (against-background (before :facts (reset-beliefs-goals))))

(fact "about eval-goal when the goal has not been reached"
  (eval-goal g2) => nil
  @current-belief => "belief 1"
  @current-goal => "goal 2"
  (against-background (before :facts (reset-beliefs-goals))))
