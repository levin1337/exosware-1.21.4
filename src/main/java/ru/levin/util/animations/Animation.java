package ru.levin.util.animations;


import ru.levin.util.player.TimerUtil;

public abstract class Animation {

    public TimerUtil timerUtil = new TimerUtil();
    protected int duration;
    protected double endPoint;
    protected net.minecraft.util.math.Direction.AxisDirection direction;

    public Animation(int ms, double endPoint) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = net.minecraft.util.math.Direction.AxisDirection.POSITIVE;
    }

    public Animation(int ms, double endPoint, net.minecraft.util.math.Direction.AxisDirection direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public boolean finished(net.minecraft.util.math.Direction.AxisDirection direction) {
        return isDone() && this.direction.equals(direction);
    }

    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }

    public void reset() {
        timerUtil.reset();
    }

    public boolean isDone() {
        return timerUtil.hasTimeElapsed(duration);
    }

    public net.minecraft.util.math.Direction.AxisDirection getDirection() {
        return direction;
    }

    public void setDirection(net.minecraft.util.math.Direction.AxisDirection direction) {
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.setTime(System.currentTimeMillis() - (duration - Math.min(duration, timerUtil.getTime())));
        }
    }
    public Animation setDirection(boolean forwards) {
        net.minecraft.util.math.Direction.AxisDirection direction = forwards ? net.minecraft.util.math.Direction.AxisDirection.POSITIVE : net.minecraft.util.math.Direction.AxisDirection.POSITIVE;
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTime(System.currentTimeMillis() - ((long)this.duration - Math.min((long)this.duration, this.timerUtil.getTime())));
        }

        return this;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (direction == net.minecraft.util.math.Direction.AxisDirection.POSITIVE) {
            if (isDone())
                return endPoint;
            return (getEquation(timerUtil.getTime()) * endPoint);
        } else {
            if (isDone()) return 0;
            if (correctOutput()) {
                double revTime = Math.min(duration, Math.max(0, duration - timerUtil.getTime()));
                return getEquation(revTime) * endPoint;
            } else return (1 - getEquation(timerUtil.getTime())) * endPoint;
        }
    }
    public double getEndput() {
        if (direction == net.minecraft.util.math.Direction.AxisDirection.NEGATIVE) {
            if (isDone())
                return endPoint;
            return (getEquation(timerUtil.getTime()) * endPoint);
        } else {
            if (isDone()) return 0;
            if (correctOutput()) {
                double revTime = Math.min(duration, Math.max(0, duration - timerUtil.getTime()));
                return getEquation(revTime) * endPoint;
            } else return (1 - getEquation(timerUtil.getTime())) * endPoint;
        }
    }

    protected abstract double getEquation(double x);
}