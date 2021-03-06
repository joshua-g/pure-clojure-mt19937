(ns ^{:doc "simple MT19937 (Mersenne Twister) implementation written in pure clojure"
      :author "Joshua Greenberg"}
  joshua-g.mersenne)

;; private declarations
(declare make-seq)
(declare wrap-value-state-tuple)
(declare m-next)


(deftype Mersenne [state]
  clojure.lang.Seqable
  (seq [this] (make-seq state))
  Object
  (equals [this other]
    (and (instance? Mersenne other)
         (let [o ^Mersenne other]
           (= state (.state o))))))

(defn mersenne
  "Create a Mersenne, which is an (immutable) Seqable that
  encapsulates the PRNG state, based on the supplied integer seed"
  [seed]
  {:pre [(integer? seed)]}
  (->Mersenne
   [624
    (->> [seed (range 1 624)]
         (apply reductions
                (fn [prev idx]
                  (->> (bit-xor prev
                                (bit-shift-right prev 30))
                       (* 1812433253)
                       (+ idx)
                       (bit-and 0xFFFFFFFF))))
         vec)]))

(defn next-value
  "Takes a Mersenne and returns a 2-tuple of the next integer
  value and the resulting Mersenne"
  [^Mersenne m]
  (-> (.state m)
      m-next
      wrap-value-state-tuple))

(defn next-values
  "Takes a Mersenne and a number n, and returns a 2-tuple of
  a vector of the next n integer values, and the resulting Mersenne"
  [n ^Mersenne m]
  (->> [[] (.state m)]
       (iterate (fn [[vs state]]
                  (let [[new-v new-state] (m-next state)]
                    [(conj vs new-v) new-state])))
       (#(nth % n))
       wrap-value-state-tuple))

(defn- wrap-value-state-tuple [[v state]]
  [v (->Mersenne state)])

(defn- make-seq [state]
  (lazy-seq
   (let [[v new-state] (m-next state)]
     (cons v (make-seq new-state)))))

(defn- twist [mt-vec]
  (->> [mt-vec (range 0 624)]
       (apply reduce
              (fn [mt-vec i]
                (let [x (mt-vec (mod (+ i 397) 624))
                      y (bit-and 0xFFFFFFFF
                                 (+ (bit-and (mt-vec i) 0x80000000)
                                    (bit-and (mt-vec (mod (inc i) 624)) 0x7FFFFFFF)))
                      z (if (zero? (bit-and y 1))
                          0
                          0x9908B0DF)]
                  (assoc mt-vec i
                         (bit-xor x (bit-shift-right y 1) z)))))))

(defn- m-next [state]
  (let [[idx mt-vec] state]
    (if (>= idx 624) (recur [0 (twist mt-vec)])
        (as-> (mt-vec idx) $
              (bit-xor $ (bit-shift-right $ 11))
              (bit-xor $ (bit-and (bit-shift-left $ 7) 2636928640))
              (bit-xor $ (bit-and (bit-shift-left $ 15) 4022730752))
              (bit-xor $ (bit-shift-right $ 18))
              (bit-and $ 0xFFFFFFFF)
              [$ [(inc idx) mt-vec]]))))
