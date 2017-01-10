package me.kooruyu.games.battlefield1648.events;

import android.os.Bundle;

public interface EventCallable {

    void setActive(boolean active);

    void trigger();

    Bundle getMetadata();
}
