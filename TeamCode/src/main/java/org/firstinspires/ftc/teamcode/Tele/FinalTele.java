package org.firstinspires.ftc.teamcode.Tele;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.*;

import org.firstinspires.ftc.teamcode.Odometry.OdometryGlobalCoordinatePosition;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOpFinal", group = "LinearOpMode")
public class FinalTele extends LinearOpMode {

    public DcMotor fl, fr, bl,br;//matthew is fat
    public DcMotor intakeR, intakeL;
    public DcMotor flywheel, flywheel1;
    public Servo mag, flap, tilt;
    public Servo in1, in2;
    public Servo arm1, arm2;
    public Servo grabber;
    public boolean ninja = false, reverse = false, magUp = false;
    public double multiplier = 1;
    public double lastTime = System.currentTimeMillis();
    public boolean flapUp = false, armUp = false;
    public OdometryGlobalCoordinatePosition position;
    public final double COUNTS_PER_INCH = 3072;
    public DcMotor verticalLeft, verticalRight, horizontal;
    public Servo rightIntakeHolder, leftIntakeHolder;
    public void runOpMode() throws InterruptedException {

        initialize();
        //position = new OdometryGlobalCoordinatePosition(fr, br, bl, COUNTS_PER_INCH, 75, 0, 0);
        position = new OdometryGlobalCoordinatePosition(bl, br, fr, COUNTS_PER_INCH, 75, 0, 0, 0);
        //position = new OdometryGlobalCoordinatePosition(br, bl, fr, COUNTS_PER_INCH, 75, 0, 0);

        Thread positionThread = new Thread(position);
        positionThread.start();
        waitForStart();

        while(opModeIsActive()){
            drive();
            wobbleArm();
            intake();
            shooter();
            telemetry.addData("X Position", position.getX() );
            telemetry.addData("Y Position", position.getY() );
            telemetry.addData("left encoder", bl.getCurrentPosition());
            telemetry.addData("right encoder", br.getCurrentPosition());
            telemetry.addData("horizontal encoder", fr.getCurrentPosition());
            telemetry.addData("Orientation (Degrees)", position.returnOrientation());
            telemetry.addData("Thread Active", positionThread.isAlive());
            telemetry.addData("flap pos:", flap.getPosition ());
            telemetry.update();
        }
        position.stop();
    }
    public void dropIntake(){
        if(gamepad1.x==true){
            leftIntakeHolder.setPosition(0.5);
            rightIntakeHolder.setPosition(1);
            sleep(200);
            leftIntakeHolder.setPosition(0.78);
            rightIntakeHolder.setPosition(0.81);
        }

    }
    public void initialize(){
        fl = hardwareMap.get(DcMotor.class , "fl"); //green
        bl = hardwareMap.get(DcMotor.class , "bl"); //red
        fr = hardwareMap.get(DcMotor.class , "fr"); //blue
        br = hardwareMap.get(DcMotor.class , "br"); //white
        intakeR = hardwareMap.get(DcMotor.class, "intakeR"); //green
        intakeL = hardwareMap.get(DcMotor.class, "intakeL"); //red
        in1 = hardwareMap.get(Servo.class, "in1");
        in2 = hardwareMap.get(Servo.class, "in2");

        intakeR.setDirection( DcMotorSimple.Direction.FORWARD);
        intakeL.setDirection(DcMotorSimple.Direction.REVERSE);
        fl.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fr.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        fl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        br.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        fl.setDirection(DcMotor.Direction.REVERSE);
        bl.setDirection(DcMotor.Direction.REVERSE);

        flywheel = hardwareMap.get(DcMotor.class, "fw");//black
        flywheel1 = hardwareMap.get(DcMotor.class, "fw1");//silver
        flywheel.setDirection(DcMotor.Direction.REVERSE);
        flywheel1.setDirection(DcMotor.Direction.REVERSE);
        mag = hardwareMap.get(Servo.class, "mag");
        flap = hardwareMap.get(Servo.class, "flap");
        tilt = hardwareMap.get(Servo.class, "tilt");
        arm1 = hardwareMap.get(Servo.class, "wobbleArm1");
        arm2 = hardwareMap.get(Servo.class, "wobbleArm2");
        leftIntakeHolder = hardwareMap.get(Servo.class,"liServo");
        rightIntakeHolder = hardwareMap.get(Servo.class,"riServo");
        arm1.setPosition(0.07);
        arm2.setPosition (0.93);
        grabber = hardwareMap.get(Servo.class, "wobbleGrabber");
        grabber.setPosition(0.92);
        mag.setPosition(0.47);
        tilt.setPosition(0.13);
        flap.setPosition(0.3);
        telemetry.addData("Status", "Initialized");
    }
    public void drive(){
        double r = Math.hypot(gamepad1.left_stick_x, -gamepad1.left_stick_y);
        double robotAngle = Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.PI / 4;
        double rightX = gamepad1.right_stick_x*0.8;
        double v1 = r * Math.cos(robotAngle)*multiplier + rightX;
        double v2 = r * Math.sin(robotAngle)*multiplier - rightX;
        double v3 = r * Math.sin(robotAngle)*multiplier + rightX;
        double v4 = r * Math.cos(robotAngle)*multiplier - rightX;
        if(ninja == false && gamepad1.left_bumper == true && System.currentTimeMillis() >= lastTime + 300){
            ninja = true;
            lastTime = System.currentTimeMillis();
        }
        else if(ninja == true && gamepad1.left_bumper == true && System.currentTimeMillis() >= lastTime + 300){
            ninja = false;
            lastTime = System.currentTimeMillis();
        }
        if(reverse == false && gamepad1.right_bumper == true && System.currentTimeMillis() >= lastTime + 300){
            reverse = true;
            lastTime = System.currentTimeMillis();
        }
        else if(reverse == true && gamepad1.right_bumper == true && System.currentTimeMillis() >= lastTime + 300){
            reverse = false;
            lastTime = System.currentTimeMillis();
        }
        if(ninja==true){
            v1 /= 3;
            v2 /= 3;
            v3 /= 3;
            v4 /= 3;
        }
        if(reverse==true){
            multiplier = -1;
        }
        else{
            multiplier = 1;
        }
        fl.setPower(v1);
        fr.setPower(v2);
        bl.setPower(v3);
        br.setPower(v4);
    }
    public void wobbleArm(){
        if(gamepad2.a == true && armUp == false){
            arm1.setPosition(0.45);
            arm2.setPosition (0.52);
            armUp = true;
            sleep(100);
        }
        else if(gamepad2.a == true && armUp == true){
            //0.07 init
            arm1.setPosition(0.91);
            arm2.setPosition (0.07);
            armUp = false;
            sleep(100);
        }

        if(gamepad2.x == true && grabber.getPosition()>0.3){
            grabber.setPosition(0.15);
            sleep(100);
        }
        else if(gamepad2.x == true && grabber.getPosition()<0.3){
            grabber.setPosition(0.43);
            sleep(100);
        }
    }
    public void intake(){
        double intakeSpeed = -gamepad2.right_stick_y;
        intakeL.setPower(intakeSpeed);
        intakeR.setPower(intakeSpeed);
        if(intakeSpeed<0){
            in1.setPosition(1);
            in2.setPosition(0);
        }
        else if(intakeSpeed>0){
            in1.setPosition(0);
            in2.setPosition(1);
        }
        else{
            in1.setPosition(0.5);
            in2.setPosition(0.5);
        }
        if(intakeSpeed!=0){
            telemetry.addData ("Intake", "on");
        }
    }
    public void shooter(){
        if(gamepad2.y==true && flapUp == false){
            flap.setPosition(0.06);
            sleep(50);
            flapUp = true;
        }
        else if(gamepad2.y==true && flapUp == true){
            flap.setPosition(0);
            sleep(50);
            flapUp = false;
        }

        if(gamepad2.right_bumper==true){
            mag.setPosition(0.28);
            sleep(150);
            mag.setPosition(.47);
        }
        if(gamepad2.left_trigger >=0.1){
            tilt.setPosition(0.31);
            flywheel.setPower(-1);
            flywheel1.setPower(-1);
        }
        else{
            tilt.setPosition(0.13);
            flywheel.setPower(0);
            flywheel1.setPower(0);

        }
        if(gamepad2.right_trigger >= 0.1){
            int i = 0;
            while(i<3){

                mag.setPosition(0.32);
                sleep(350);
                mag.setPosition(.47);
                sleep(350);
                i++;
                telemetry.addData("i", i);
                telemetry.update();

            }
        }
    }
}
