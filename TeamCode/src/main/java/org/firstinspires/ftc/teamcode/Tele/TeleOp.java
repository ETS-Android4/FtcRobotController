package org.firstinspires.ftc.teamcode.Tele;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Components.PIDController;
import org.firstinspires.ftc.teamcode.Components.Robot;
import org.firstinspires.ftc.teamcode.Odometry.OdometryGlobalCoordinatePosition;
import org.firstinspires.ftc.teamcode.PurePursuit.Coordinate;

import static org.firstinspires.ftc.teamcode.PurePursuit.MathFunctions.AngleWrap;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOp", group = "LinearOpMode")
public class TeleOp extends LinearOpMode implements Runnable{
    Robot robot;
    boolean ninja, armUp;
    Coordinate shootingPosition;
    double shootingAngle = 0;
    boolean isOnAuto = false;
    PIDController pidRotate;
    boolean isWingsOut = false;
    double shootSpeed = 0.85;
    boolean hasShot = false;
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(DcMotor.RunMode.RUN_WITHOUT_ENCODER, hardwareMap, 18, 18, () -> opModeIsActive() && gamepadIdle());
        robot.init();
        pidRotate = new PIDController(.07, 0.014, 0.0044);
        Thread driveTrain = new Thread(this);
        waitForStart();
        shootingPosition = Robot.position.toPoint();
        driveTrain.start();

        while(opModeIsActive()){
            setMultiplier();
            wobble();
            robot.intake(gamepad2.right_stick_y);
            shooter();
            idle();
            if(gamepad2.y || gamepad1.right_trigger >= 0.2){
                if(!isWingsOut) {
                    robot.wingsOut();
                    isWingsOut = true;
                    sleep(500);
                } else {
                    robot.wingsIn();
                    isWingsOut = false;
                    sleep(500);
                }
            }
        }
        driveTrain.stop();
        Robot.position.stop();
    }
    double lxMult = 1;
    double lyMult = 1;
    double rxMult = 1;
    @Override
    public void run() {
        while(opModeIsActive()) {
            robot.setMovement(gamepad1.left_stick_x * lxMult, -gamepad1.left_stick_y * lyMult, gamepad1.right_stick_x * rxMult);
            if(gamepad1.y) {
                robot.goTo(shootingPosition, 0.8, shootingAngle, 0.6);
            }
            telemetry.addData("X Position", robot.position.getX());
            telemetry.addData("Y Position", robot.position.getY());
            telemetry.addData("Orientation (Degrees)", robot.position.returnOrientation());
            telemetry.update();
        }
    }
    private void setMultiplier(){
        if(!ninja && gamepad1.left_bumper){
            ninja = true;
            lxMult /= 3;
            rxMult /= 3;
            lyMult /= 3;
            sleep(300);
        }
        else if(ninja && gamepad1.left_bumper){
            ninja = false;
            lxMult *= 3;
            rxMult *= 3;
            lyMult *= 3;
            sleep(300);
        }
        if(gamepad1.right_bumper){
            lxMult = -lxMult;
            lyMult = -lyMult;
            sleep(300);
        }
    }
    public void wobble(){
        if(gamepad2.b && !armUp){
            robot.wobbleArmUp();
            armUp = true;
            sleep(300);
        }
        else if(gamepad2.b && armUp){
            robot.wobbleArmDown();
            armUp = false;
            sleep(300);
        }
        else if(gamepad2.a){
            robot.wobbleArmVertical();
            armUp = false;
        }
        if(gamepad2.x && robot.grabber.getPosition()>0.3){
            robot.grab();
            sleep(300);
        }
        else if(gamepad2.x && robot.grabber.getPosition()<0.3){
            robot.release();
            sleep(300);
        }
    }
    private void autoPowerShots(){
        for(Coordinate pwrShot : Robot.pwrShots){
            robot.turnTo(pwrShot, 0.24);
        }
    }
    public void shooter(){
        if(gamepad2.left_trigger >=0.1){
            if(!hasShot){
                shootSpeed = 0.68;
                hasShot = true;
            }
            isWingsOut = true;
            robot.launcher.setFlyWheel(shootSpeed);
            //robot.wingsOut();
            robot.launcher.flapUp();
        }
        else if(gamepad2.left_bumper){
            isWingsOut = true;
            robot.launcher.setFlyWheel(0.4);
            robot.wingsOut();
            robot.launcher.flapDown();
        }
        else {
            hasShot = false;
            robot.launcher.setFlyWheel(0);
            if(!isWingsOut)
                robot.wingsIn();
            robot.launcher.flapDown();
        }
        if(gamepad2.right_bumper){
            robot.launcher.singleRound();
            shootSpeed = 0.71;
            storeCoordinate();
        }
        if(gamepad2.right_trigger >= 0.1){
            robot.launcher.magazineShoot();
            storeCoordinate();
        }
    }
    private void storeCoordinate(){
        shootingPosition = Robot.position.toPoint();
        shootingAngle = Robot.position.radians();
    }
    private boolean gamepadIdle(){
        return gamepad1.left_stick_x == 0 && gamepad1.left_stick_y == 0 && gamepad1.right_stick_x == 0;
    }
    private void goToLaunchZone(){
        robot.goTo(shootingPosition, 0.5, shootingAngle, 0.5);
    }
}
