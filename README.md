# vector-field-demo
This is an interactive Java application to demonstrate what vector fields are and how they work.

Uses [exp4j](http://projects.congrace.de/exp4j/download.html), a free library for evaluating functions.

## Features
* Visualize vector field of arbitrary 2D vector-valued function
* Click to drop a point that "flows" through the field
* Click and hold, dragging a rectangular region, to drop a rectangle that will also "flow" through the field
* When there is a point, see instantaneous values (Values tab)
* Change window size to better visualize function

## Controls
Function
> As a 2D vector-valued function, it has an i-vector component and a j-vector component, both of which are functions of x and y.

> Any valid function of x and y will be accepted, including trigonometric functions; anything that [exp4j supports](http://www.objecthunter.net/exp4j/#Built-in_functions).

> After changing the function, you must press the "Update Vector Field" button for the visualization to update.

Speed
> Controls the internal delay used between updating the position of the point (or points, in the case of a rectangle) when having dropped one onto the field.

> For cases where larger amounts of computation is involved, changing the speed bar may have little effect; consider changing the resolution instead (see below).

Resolution
> Points are essentially moved through approximating a differential by taking very small "steps" according to the current vector, resolution determines the size of these steps.

> A higher resolution will make it more precise, and will give more accuracy according to the path that should be expected, although this is likely not noticeable.

> A lower resolution may cause noticeable deviance from expected behavior in certain cases, but often will not make a difference. Lowering resolution can also be very useful when computation load is high (such as dropping a rectangle), and may grant a considerable speed boost.

## Screenshots
<img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_01.png" alt="screenshot_01" height="300"/><img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_02.png" alt="screenshot_02" height="300"/>
<img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_03.png" alt="screenshot_03" height="300"/><img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_04.png" alt="screenshot_04" height="300"/>
<img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_05.png" alt="screenshot_05" height="300"/><img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_06.png" alt="screenshot_06" height="300"/>
<img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_07.png" alt="screenshot_07" height="300"/><img src="https://raw.githubusercontent.com/shrucis1/vector-field-demo/master/screenshots/screenshot_08.png" alt="screenshot_08" height="300"/>

