(in-ns 'play-clj.core)

; drawing

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil
  [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod sprite-batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (sprite-batch screen)]
    (.begin batch)
    (doseq [e entities]
      (cond
        (map? e)
        (let [{:keys [^TextureRegion image x y width height]} e]
          (.draw batch image (float x) (float y) (float width) (float height)))
        (isa? (type e) Actor)
        (.draw ^Actor e batch 1)))
    (.end batch))
  entities)

; textures

(defn image
  [val]
  (if (string? val)
    (-> ^String val Texture. TextureRegion.)
    (TextureRegion. ^TextureRegion val)))

(defn image-width
  ([^TextureRegion img]
    (.getRegionWidth img))
  ([img val]
    (doto ^TextureRegion (image img) (.setRegionWidth val))))

(defn image-height
  ([^TextureRegion img]
    (.getRegionHeight img))
  ([img val]
    (doto ^TextureRegion (image img) (.setRegionHeight val))))

(defn split-image
  ([val size]
    (split-image val size size))
  ([val width height]
    (-> val ^TextureRegion image (.split width height))))

(defn flip-image
  [val x? y?]
  (doto ^TextureRegion (image val) (.flip x? y?)))

(defmacro animation
  [duration images & args]
  `(Animation. ~duration
               (utils/gdx-into-array ~images)
               (utils/gdx-static-field :graphics :g2d :Animation
                                       ~(or (first args) :normal))))

(defn get-key-frame
  ([screen ^Animation animation]
    (.getKeyFrame animation (:total-time screen) true))
  ([screen ^Animation animation is-looping?]
    (.getKeyFrame animation (:total-time screen) is-looping?)))
