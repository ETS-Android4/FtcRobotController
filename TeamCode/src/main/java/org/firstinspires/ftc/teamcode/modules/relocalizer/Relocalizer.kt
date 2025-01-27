package org.firstinspires.ftc.teamcode.modules.relocalizer

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.modules.Module
import org.firstinspires.ftc.teamcode.modules.relocalizer.distancesensor.UltrasonicDistanceSensor
import org.firstinspires.ftc.teamcode.roadrunnerext.geometry.polarAdd
import org.firstinspires.ftc.teamcode.util.controllers.MovingMedian
import org.firstinspires.ftc.teamcode.util.field.Alliance
import org.firstinspires.ftc.teamcode.util.field.Context
import org.firstinspires.ftc.teamcode.util.field.Context.alliance
import org.firstinspires.ftc.teamcode.util.field.Context.pitch
import org.firstinspires.ftc.teamcode.util.field.Context.tilt
import javax.lang.model.type.NullType
import kotlin.math.cos
import kotlin.math.sin

class Relocalizer(hardwareMap: HardwareMap) : Module<NullType?>(hardwareMap, null) {
    enum class Sensor {
        FRONT_LEFT,
        FRONT_RIGHT,
        LEFT,
        RIGHT
    }
    private val frontLeftDistance =
        UltrasonicDistanceSensor(
            hardwareMap ,
            "leftFrontDS",
            UltrasonicDistanceSensor.SensorType.LongRange,
            Pose2d(7.6, 7.25, Math.toRadians(0.0))
        )
    private val frontRightDistance = UltrasonicDistanceSensor(
        hardwareMap ,
        "rightFrontDS",
        UltrasonicDistanceSensor.SensorType.LongRange,
        Pose2d(7.6, -7.25, Math.toRadians(-0.0))
    )
    private val leftDistance = UltrasonicDistanceSensor(
        hardwareMap ,
        "leftDS",
        UltrasonicDistanceSensor.SensorType.ShortRange,
        Pose2d(-3.75, 8.2, Math.toRadians(90.0))
    )
    private val rightDistance = UltrasonicDistanceSensor(
        hardwareMap ,
        "rightDS",
        UltrasonicDistanceSensor.SensorType.ShortRange,
        Pose2d(-3.75, -8.2, Math.toRadians(-90.0))
    )

    val poseEstimate: Pose2d
        get() = Pose2d(x, y, heading)
    private var x = 0.0
    private var y = 0.0
    private var heading = 0.0

    override fun internalInit() {
        setNestedModules(frontLeftDistance, frontRightDistance, leftDistance, rightDistance)
    }

    private fun getSensor(sensor: Sensor?): UltrasonicDistanceSensor? {
        return when (sensor) {
            Sensor.FRONT_LEFT -> frontLeftDistance
            Sensor.FRONT_RIGHT -> frontRightDistance
            Sensor.LEFT -> leftDistance
            Sensor.RIGHT -> rightDistance
            else -> null
        }
    }

    private val samplingX = MovingMedian(3)
    private val samplingY = MovingMedian(3)

    fun updatePoseEstimate(xCorrection: Sensor?, yCorrection: Sensor?) {
        val xSensor = getSensor(xCorrection)
        val ySensor = getSensor(yCorrection)
        val frontWallX = 70.5
        val sideWallY = if (alliance == Alliance.BLUE) 70.5 else -70.5
        val heading = Context.robotPose.heading
        val rawXDist = xSensor?.getDistance(DistanceUnit.INCH) ?: 0.0
        val rawYDist = ySensor?.getDistance(DistanceUnit.INCH) ?: 0.0
        val filteredX = samplingX.update(rawXDist)
        val filteredY = rawYDist
        val xDist = xSensor?.let { filteredX * cos(sin(it.poseOffset.heading) * tilt + cos(it.poseOffset.heading) * pitch) } ?: 0.0
        val yDist = ySensor?.let { filteredY * cos(sin(it.poseOffset.heading) * tilt + cos(it.poseOffset.heading) * pitch) } ?: 0.0

        Context.packet.put("YDIST", yDist)
        Context.packet.put("XDIST", xDist)
        Context.packet.put("RAW_XDIST", rawXDist)
        Context.packet.put("RAW_YDIST", rawYDist)
        Context.packet.put("Filter_XDIST", filteredX)
        Context.packet.put("Filter_YDIST", filteredY)

        val x = xSensor?.let { frontWallX - xDist * cos(it.modulePoseEstimate.heading) } ?: 0.0
        val y = ySensor?.let { sideWallY - yDist * cos(it.modulePoseEstimate.heading - Math.PI / 2) } ?: 0.0
        val xPoseEstimate =
            Pose2d(x, Context.robotPose.y, heading)
                .polarAdd(-(xSensor?.poseOffset?.x ?: 0.0))
                .polarAdd(-(xSensor?.poseOffset?.y ?: 0.0), Math.toRadians(90.0))
        val yPoseEstimate =
            Pose2d(Context.robotPose.x, y, heading)
                .polarAdd(-(ySensor?.poseOffset?.x ?: 0.0))
                .polarAdd(-(ySensor?.poseOffset?.y ?: 0.0), Math.toRadians(90.0))
        if (xCorrection != null) this.x = xPoseEstimate.x
        if (yCorrection != null) this.y = yPoseEstimate.y
        this.heading = heading
    }

    override fun internalUpdate() {
//        if (alliance == Alliance.RED) {
//            updatePoseEstimate(Sensor.FRONT_LEFT, Sensor.RIGHT)
//        } else {
//            updatePoseEstimate(Sensor.FRONT_RIGHT, Sensor.LEFT)
//        }
    }

    override fun isDoingInternalWork() = false

    override fun isTransitioningState() = false
}