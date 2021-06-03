
package org.firstinspires.ftc.teamcode.util

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.arcrobotics.ftclib.geometry.Rotation2d
import com.arcrobotics.ftclib.kinematics.wpilibkinematics.ChassisSpeeds

fun com.arcrobotics.ftclib.geometry.Pose2d.toRR(): Pose2d = Pose2d(this.translation.y, this.translation.x, this.heading)

fun Pose2d.toFTCLib(): com.arcrobotics.ftclib.geometry.Pose2d = com.arcrobotics.ftclib.geometry.Pose2d(-this.x, -this.y, Rotation2d(this.heading))

val com.arcrobotics.ftclib.geometry.Pose2d.x: Double
    get() = this.translation.x

val com.arcrobotics.ftclib.geometry.Pose2d.y: Double
    get() = this.translation.y

val com.arcrobotics.ftclib.geometry.Pose2d.rotationDeg: Double
    get() = this.rotation.degrees

val com.arcrobotics.ftclib.geometry.Pose2d.rotationRad: Double
    get() = this.rotation.radians

fun ChassisSpeeds.toRRPose2d(): Pose2d = Pose2d(this.vxMetersPerSecond, this.vyMetersPerSecond, this.omegaRadiansPerSecond)

fun Double.toRadians() = Math.toRadians(this)
fun Double.toDegrees() = Math.toDegrees(this)
fun Pose2d.toInches() = Pose2d(this.x * 39.3701, this.y * 39.701, heading)
fun Pose2d.toMeters() = Pose2d(this.x / 39.3701, this.y / 39.701, heading)
fun com.arcrobotics.ftclib.geometry.Pose2d.toMeters() = com.arcrobotics.ftclib.geometry.Pose2d(this.x / 39.3701, this.y / 39.701, Rotation2d(this.heading))

