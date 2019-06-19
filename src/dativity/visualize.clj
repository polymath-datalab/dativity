(ns dativity.visualize
  (:require
    [ubergraph.core :as uber]
    [ysera.error :refer [error]]
    [ysera.test :refer [is=]]))

(defn add-visuals-to-node
  {:test (fn []
           (is= (add-visuals-to-node {:type :data}) {:type  :data
                                                     :color :green}))}
  [node]
  (assoc node :color (condp = (:type node)
                       :action :blue
                       :data :green
                       :role :orange
                       (error (format "could not add visuals to %s" node)))))

(defn add-visuals-to-edge
  {:test (fn []
           (is= (add-visuals-to-edge {:association :produces}) {:association :produces
                                                                :color       :green
                                                                :label       "produces"}))}
  [edge]
  (condp = (:association edge)
    :produces (-> edge
                  (assoc :color :green)
                  (assoc :label "produces"))
    :requires (-> edge
                  (assoc :color :red)
                  (assoc :label "requires"))
    :requires-conditional (-> edge
                              (assoc :color :purple)
                              (assoc :label "requires?"))
    :performs (-> edge
                  (assoc :color :orange)
                  (assoc :label "does"))
    (error (format "could not add visuals to %s" edge))))

(defn- to-uber
  [graph]
  (let [nodes (->> (:nodes graph)
                   (map (fn [[k v]] [k (add-visuals-to-node v)]))
                   (vec))
        edges (->> (:edges graph)
                   (map (fn [[[src dest] v]] [src dest (add-visuals-to-edge v)]))
                   (vec))]
    (uber/edn->ubergraph {:nodes            nodes
                          :directed-edges   edges
                          :allow-parallel?  false
                          :undirected?      false
                          :undirected-edges []})))

;layouts that make sense are :fdp and :dot
(defn generate-png
  "requires graphviz"
  [graph]
  (uber/viz-graph (to-uber graph) {:layout :fdp}))
