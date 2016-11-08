package me.kooruyu.games.battlefield1648.animations;

import android.animation.TypeEvaluator;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public class VertexAnimator implements TypeEvaluator {


    @Override
    public Object evaluate(float fraction, Object start, Object end) {
        Vertex startVertex = (Vertex) start;
        Vertex endVertex = (Vertex) end;
        return new Vertex(
                (int) (startVertex.getX() + fraction * (endVertex.getX() - startVertex.getX())),
                (int) (startVertex.getY() + fraction * (endVertex.getY() - startVertex.getY())));
    }
}
