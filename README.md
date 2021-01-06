# FXGL-FastRender
A quick demo of various approaches of rendering a single pixel (`fillRect(x, y, 1, 1)`) inside an FXGL environment.
It should be noted that the results below are just a single pass (no comprehensive statistics applied). Depending on the actual use case (e.g. `drawImage()` or `drawPolygon()`), the results of these approaches might differ.

## Run

```
mvn javafx:run
```

## Some sample tests (how long it took to compute a frame in ms)

- CanvasTestApp

Note: can only run in 1 thread due to Canvas limitations.

```
Avg: 68.7463457
Min: 59.0025
Max: 106.2289
```

- CanvasIntBufferTestApp

Note: using its int buffer for speed _and_ having high-level API makes this the most desirable approach.

```
Avg: 10.3398753
Min: 8.0649
Max: 23.9596
```

- PixelBufferTestApp

Note: there seem to be some flickering issues if fps is low. In addition, no high-level API.

```
Avg: 10.5120373
Min: 7.818
Max: 21.9998
```

- AWTImageTestApp

Note: better performance than high-level Canvas but slower than Canvas with IntBuffer.

```
Avg: 45.6501834
Min: 41.524
Max: 78.3333
```