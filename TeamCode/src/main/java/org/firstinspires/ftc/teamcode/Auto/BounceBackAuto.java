package org.firstinspires.ftc.teamcode.Auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Components.Async;
import org.firstinspires.ftc.teamcode.Components.OpModeType;
import org.firstinspires.ftc.teamcode.Components.Robot;
import org.firstinspires.ftc.teamcode.PurePursuit.Coordinate;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;

import java.util.Arrays;

@Autonomous(name = "newAuto", group = "LinearOpMode")
public class BounceBackAuto extends LinearOpMode {
    Robot robot;
    Vector2d dropZone;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap, 9, 48, 0, OpModeType.auto, this);
        //robot.autoInit();
        Trajectory powerShotsTraj1 = robot.driveTrain.trajectoryBuilder(Robot.robotPose)
                .addTemporalMarker(1, () -> robot.launcher.leftOut())
                .lineToSplineHeading(new Pose2d(-40.4725, -21))
                .splineToSplineHeading(Coordinate.toPose(Robot.pwrShotLocals[2], 0), 0, new MinVelocityConstraint(
                                Arrays.asList(
                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                        new MecanumVelocityConstraint(42, DriveConstants.TRACK_WIDTH)
                                )
                        ),
                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .addDisplacementMarker(() -> {
                    Async.start(() -> robot.launcher.singleRound());
                    robot.launcher.rightOut();
                })
                .splineToConstantHeading(Robot.pwrShotLocals[1], 0, new MinVelocityConstraint(
                                Arrays.asList(
                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                        new MecanumVelocityConstraint(8.7, DriveConstants.TRACK_WIDTH)
                                )
                        ),
                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .addDisplacementMarker(() -> Async.start(() -> robot.launcher.singleRound()))
                .splineToConstantHeading(Robot.pwrShotLocals[0], 0, new MinVelocityConstraint(
                                Arrays.asList(
                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                        new MecanumVelocityConstraint(8.7, DriveConstants.TRACK_WIDTH)
                                )
                        ),
                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .addDisplacementMarker(() -> Async.start(() -> robot.launcher.singleRound()))
                .build();
//        Trajectory powerShotsTraj2 = robot.driveTrain.trajectoryBuilder(powerShotsTraj1.end())
//                .splineToLinearHeading(Coordinate.toPose(Robot.pwrShotLocals[1], 0), Math.toRadians(90), new MinVelocityConstraint(
//                                Arrays.asList(
//                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
//                                        new MecanumVelocityConstraint(15, DriveConstants.TRACK_WIDTH)
//                                )
//                        ),
//                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
//                .addDisplacementMarker(() -> Async.start(() -> robot.launcher.singleRound()))
//                .build();
//        Trajectory powerShotsTraj3 = robot.driveTrain.trajectoryBuilder(powerShotsTraj2.end())
//                .splineToLinearHeading(Coordinate.toPose(Robot.pwrShotLocals[0], 0), Math.toRadians(90), new MinVelocityConstraint(
//                                Arrays.asList(
//                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
//                                        new MecanumVelocityConstraint(15, DriveConstants.TRACK_WIDTH)
//                                )
//                        ),
//                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
//                .addDisplacementMarker(() -> Async.start(() -> robot.launcher.singleRound()))
//                .build();
        Trajectory wobbleDrop = robot.driveTrain.trajectoryBuilder(powerShotsTraj1.end())
                .addTemporalMarker(0.3, () -> {
                    robot.launcher.wingsVert();
                    Async.set(() -> Robot.C.distTo(robot.driveTrain.getPoseEstimate().vec()) <= 15, () -> robot.wobbleArmDown());
                })
                .splineTo(new Vector2d(30.5275, 8.7), Math.toRadians(0))
                .splineTo(new Vector2d(58.4, 0.5275), Math.toRadians(-90))
                .splineTo(new Vector2d(58.4, -10.4725), Math.toRadians(-90))
                .splineTo(Robot.C, Math.toRadians(-180))
                .addDisplacementMarker(() -> {
                    Async.start(() -> {
                        robot.release();
                        sleep(400);
                        robot.wobbleArmUp();
                    });
                    robot.intake(0);
                    robot.launcher.setLauncherVelocity(robot.getPoseVelo(Robot.shootingPose));
                    robot.launcher.flapDown();
                    robot.launcher.safeLeftOut();
                })
                .build();
        Trajectory firstShot = robot.driveTrain.trajectoryBuilder(wobbleDrop.end())
                .lineToLinearHeading(Robot.shootingPose)
                .addDisplacementMarker(() -> Async.start(() -> {
                    telemetry.addData("rings", robot.launcher.getRings());
                    telemetry.addData("distance", Robot.goal.distTo(Robot.shootingPose.vec()));
                    telemetry.update();
                }))
                .build();
        Trajectory wobblePickup = robot.driveTrain.trajectoryBuilder(firstShot.end())
                .lineToSplineHeading(Coordinate.toPose(Robot.rightWobble, 0), new MinVelocityConstraint(
                                Arrays.asList(
                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                        new MecanumVelocityConstraint(40, DriveConstants.TRACK_WIDTH)
                                )
                        ),
                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .addDisplacementMarker(() -> robot.launcher.setVelocity(robot.getPoseVelo(new Vector2d(-32.4725, Robot.goal.getY()))))
                .addDisplacementMarker(() -> robot.intake(1))
                .splineToConstantHeading(new Vector2d(-33, Robot.goal.getY()), 0)
                .splineToLinearHeading(new Pose2d(-25.4725, Robot.goal.getY(), 0), 0, new MinVelocityConstraint(
                                Arrays.asList(
                                        new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                        new MecanumVelocityConstraint(8, DriveConstants.TRACK_WIDTH)
                                )
                        ),
                        new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                .build();
        Trajectory finalShot = robot.driveTrain.trajectoryBuilder(wobblePickup.end())
                .addDisplacementMarker(() -> robot.intake(1))
                .lineToLinearHeading(Robot.shootingPoseTele)
                .addDisplacementMarker(() -> {
                    robot.launcher.magUp();
                    robot.intake(0);
                })
                .build();
        Trajectory wobbleDrop2 = robot.driveTrain.trajectoryBuilder(finalShot.end())
                .addDisplacementMarker(() -> Async.set(() -> robot.driveTrain.getPoseEstimate().vec().distTo(Robot.C) <= 15, () -> robot.wobbleArmDown()))
                .lineToLinearHeading(Coordinate.toPose(Robot.C, Math.toRadians(180)))
                .addDisplacementMarker(() -> Async.start(() -> {
                    robot.release();
                    sleep(60);
                    robot.wobbleArmUp();
                }))
                .build();
        Trajectory park = robot.driveTrain.trajectoryBuilder(wobbleDrop2.end())
                .splineTo(new Vector2d(9.5275, wobbleDrop2.end().getY()), Math.toRadians(180))
                .build();
        sleep(700);
        telemetry.addData("Initialization", "Complete");
        telemetry.update();
        robot.launcher.magUp();
        robot.grab();
        Thread shooterThread = new Thread(() -> {
            while (opModeIsActive()) {
                robot.launcher.updatePID();
                Robot.robotPose = robot.driveTrain.getPoseEstimate();
            }
        });
//        while (!opModeIsActive()) {
//            robot.scan();
//            telemetry.addData("Stack Height", robot.height);
//            telemetry.addData("Discs", robot.dice.toString());
//            telemetry.update();
//        }
        waitForStart();
        shooterThread.start();
        robot.turnOffVision();
        robot.launcher.flapUp();
        robot.wobbleArmUp();
        robot.launcher.setLauncherVelocity(908);
        robot.unlockIntake();
        robot.driveTrain.followTrajectory(powerShotsTraj1);
//        robot.driveTrain.followTrajectory(powerShotsTraj2);
//        robot.driveTrain.followTrajectory(powerShotsTraj3);
        sleep(100);
        robot.launcher.setLauncherVelocity(0);
        robot.intake(1);
        robot.driveTrain.followTrajectory(wobbleDrop);
        /*
        PICK UP BOUNCEBACKS
         */
        robot.driveTrain.followTrajectory(firstShot);
        robot.wobbleArmDown();
        robot.launcher.safeShoot();
        robot.launcher.setLauncherVelocity(0);
        robot.wobbleArmDown();
        Async.set(() -> robot.driveTrain.getPoseEstimate().vec().distTo(Robot.rightWobble) <= 15, () -> {
            robot.grab();
            sleep(600);
            robot.wobbleArmUp();
        });
        robot.driveTrain.followTrajectory(wobblePickup);
        sleep(600);
        robot.launcher.magUp();
        robot.intake(0);
        sleep(130);
        robot.launcher.setVelocity(robot.getPoseVelo(Robot.shootingPoseTele));
        robot.launcher.singleRound();
        sleep(60);
        robot.launcher.magDown();
        robot.launcher.leftOut();
        robot.driveTrain.followTrajectory(finalShot);
        sleep(60);
        robot.launcher.magazineShoot();
        sleep(60);
        robot.launcher.rightOut();
        robot.launcher.setLauncherVelocity(0);
        robot.driveTrain.followTrajectory(wobbleDrop2);
        robot.driveTrain.followTrajectory(park);
    }
}
