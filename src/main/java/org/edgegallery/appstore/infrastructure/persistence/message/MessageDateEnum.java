package org.edgegallery.appstore.infrastructure.persistence.message;


public enum MessageDateEnum {
    TODAY(0),
    WEEK(-7),
    MONTH(-30);

    public final int dayValue;

    private MessageDateEnum(int dayValue) {
        this.dayValue = dayValue;
    }

}
