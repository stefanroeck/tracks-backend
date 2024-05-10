package de.sroeck.tracksbackend.gpxreduce


data class Point<T>(val x: Double, val y: Double, val source: T)

// implement Douglas-Peucker algorithm, see https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
fun <T> douglasPeucker(points: List<Point<T>>, epsilon: Double): List<Point<T>> {
    if (points.size < 3) {
        return points
    }

    val dmax = points.map { point -> perpendicularDistance(point, points.first(), points.last()) }.maxOrNull() ?: 0.0

    if (dmax > epsilon) {
        val index =
            points.indexOf(points.maxByOrNull { point -> perpendicularDistance(point, points.first(), points.last()) })
        val firstLine = points.subList(0, index + 1)
        val lastLine = points.subList(index, points.size)
        val firstReduced = douglasPeucker(firstLine, epsilon)
        val lastReduced = douglasPeucker(lastLine, epsilon)
        return firstReduced.dropLast(1) + lastReduced
    } else {
        return listOf(points.first(), points.last())
    }
}

private fun <T> perpendicularDistance(point: Point<T>, first: Point<T>, last: Point<T>): Double {
    val x0 = point.x
    val y0 = point.y
    val x1 = first.x
    val y1 = first.y
    val x2 = last.x
    val y2 = last.y

    val numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1)
    val denominator = Math.sqrt(Math.pow(y2 - y1, 2.0) + Math.pow(x2 - x1, 2.0))
    return numerator / denominator
}
