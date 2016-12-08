package me.kooruyu.games.battlefield1648.animations;

import android.animation.TypeEvaluator;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public class VertexAnimator implements TypeEvaluator {


    @Override
    public Object evaluate(float fraction, Object start, Object end) {
        Vertex startVertex = (Vertex) start;
        Vertex endVertex = (Vertex) end;
        return new Vertex(
                (int) (endVertex.getX() + fraction * (startVertex.getX() - endVertex.getX())),
                (int) (endVertex.getY() + fraction * (startVertex.getY() - endVertex.getY())));
    }
}
