package org.firstinspires.ftc.teamcode.modules.vision;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@TeleOp(name = "TSEVision Test")
public class TestOpMode extends OpMode {
    private static final int CAMERA_WIDTH = 320; // width  of wanted camera resolution
    private static final int CAMERA_HEIGHT = 240; // height of wanted camera resolution
    public OpenCvWebcam webcam;
    private TSEDetector TSEDetector;
    FtcDashboard dashboard = FtcDashboard.getInstance();

    @Override
    public void init() {
        int cameraMonitorViewId = this
                .hardwareMap
                .appContext
                .getResources().getIdentifier(
                        "cameraMonitorViewId",
                        "id",
                        hardwareMap.appContext.getPackageName()
                );
        webcam = OpenCvCameraFactory
                .getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        webcam.setPipeline(TSEDetector = new TSEDetector());

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT);
            }
            public void onError(int errorCode) {
            }
        });

        dashboard.startCameraStream(webcam, 30);

        telemetry.addLine("waiting for start");
        telemetry.update();
    }

    @Override
    public void loop() {
        telemetry.addData("Location", TSEDetector.getLocation());
        telemetry.update();
    }
}
