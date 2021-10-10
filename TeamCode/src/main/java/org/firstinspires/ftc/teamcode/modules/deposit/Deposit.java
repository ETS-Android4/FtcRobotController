package org.firstinspires.ftc.teamcode.modules.deposit;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.modules.Module;
import org.firstinspires.ftc.teamcode.util.BPIDFController;
/**
 * @author Martin Xu
 */

public class Deposit extends Module<Deposit.State> {
    enum State {
        LEVEL3(3, 6),
        LEVEL2(2, 4),
        LEVEL1(1, 2),
        IDLE(0, 0);
        final double dist;
        final double time;
        State(double dist, double time) {
            this.dist = dist;
            this.time = time;

        }
    }
    DcMotorEx slides;
    Platform platform;

    public static PIDCoefficients MOTOR_PID = new PIDCoefficients(0,0,0);
    public static double kV = 0;
    public static double kA = 0;
    public static double kStatic = 0;
    public static double integralBand = Double.POSITIVE_INFINITY;
    BPIDFController pidController = new BPIDFController(MOTOR_PID, integralBand, kV, kA, kStatic);

    double lastKv = kV;
    double lastKa = kA;
    double lastKStatic = kStatic;
    double lastIntegralBand = integralBand;
    double lastKp = MOTOR_PID.kP;
    double lastKi = MOTOR_PID.kI;;
    double lastKd = MOTOR_PID.kD;

    final double ANGLE_DEGREES = 30;
    ElapsedTime elapsedTime;

    /**
     * Constructor which calls the 'init' function
     *
     * @param hardwareMap instance of the hardware map provided by the OpMode
     */
    public Deposit(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    /**
     * This function initializes all necessary hardware modules
     */
    @Override
    public void init() {
        platform = new Platform(hardwareMap);
        slides = hardwareMap.get(DcMotorEx.class, "linearSlide");
    }

    /**
     * This function updates all necessary controls in a loop
     */
    @Override
    public void update() {
        pidController.setTargetPosition(getNeededDistance(getState().dist));
        pidController.update(slides.getCurrentPosition(), getHorizontalVelocity());

        if (kV != lastKv || kA != lastKa || kStatic != lastKStatic || integralBand != lastIntegralBand || MOTOR_PID.kP != lastKp || MOTOR_PID.kI != lastKi || MOTOR_PID.kD != lastKd) {
            lastKv = kV;
            lastKa = kA;
            lastKStatic = kStatic;
            lastIntegralBand = integralBand;
            lastKp = MOTOR_PID.kP;
            lastKi = MOTOR_PID.kI;
            lastKd = MOTOR_PID.kD;

            pidController = new BPIDFController(MOTOR_PID, integralBand, kV, kA, kStatic);
        }
        if(getHorizontalVelocity() == 0){
            setState(State.IDLE);
        } else if(getHorizontalVelocity() > 0 && getNeededDistance(getState().dist) == 20 /*distance for level 1 here*/){
            setState(State.LEVEL1);
        } else if(getHorizontalVelocity() > 0 && getNeededDistance(getState().dist) == 40 /*distance for level 2 here*/){
            setState(State.LEVEL2);
        } else if(getHorizontalVelocity() > 0 && getNeededDistance(getState().dist) == 60 /*distance for level 3 here*/){
            setState(State.LEVEL3);
        }
        elapsedTime.startTime();
        if (getState() == State.LEVEL1) {
            platform.update(); //ready position for going up
            slides.setTargetPosition(20 /*distance for level 1 here*/);
            slides.setVelocityPIDFCoefficients(lastKp, lastKi, lastKd, lastKStatic);
            platform.update(); //tilt position
            slides.setTargetPosition(0 /*distance for bottom here*/);
            platform.update(); //returns to orig. position for intake
        } else if (getState() == State.LEVEL2) {
            platform.update(); //ready position for going up
            slides.setTargetPosition(40 /*distance for level 2 here*/);
            slides.setVelocityPIDFCoefficients(lastKp, lastKi, lastKd, lastKStatic);
            platform.update(); //tilt position
            slides.setTargetPosition(0 /*distance for bottom here*/);
            platform.update(); //returns to orig. position for intake
        } else if (getState() == State.LEVEL3) {
            platform.update(); //ready position for going up
            slides.setTargetPosition(60 /*distance for level 3 here*/);
            slides.setVelocityPIDFCoefficients(lastKp, lastKi, lastKd, lastKStatic);
            platform.update(); //tilt position
            slides.setTargetPosition(0 /*distance for bottom here*/);
            platform.update(); //returns to orig. position for intake
        }
        elapsedTime.reset();

    }

    private double getHorizontalVelocity() {
        return slides.getVelocity() * Math.cos(Math.toRadians(ANGLE_DEGREES));
    }

    /**
     * @param dist The wanted vertical distance
     * @return The needed distance for the slides to travel in order to achieve a particular vertical distance
     */
    private double getNeededDistance(double dist) {
        return dist / Math.sin(Math.toRadians(ANGLE_DEGREES));
    }

    /**
     * @return Whether the module is currently in a potentially hazardous state for autonomous to resume
     */
    @Override
    public boolean isHazardous() {
        if(getState() != State.IDLE && elapsedTime.time() > getState().time){
            return true;
        }
        return false;
    }
}
