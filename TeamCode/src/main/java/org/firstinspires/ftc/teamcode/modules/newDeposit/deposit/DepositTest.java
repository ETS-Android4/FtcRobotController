package org.firstinspires.ftc.teamcode.modules.newDeposit.deposit;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.gamepad.ToggleButtonReader;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.modules.ModuleTest;
import org.firstinspires.ftc.teamcode.modules.intake.Intake;
import org.firstinspires.ftc.teamcode.util.field.Balance;

import static org.firstinspires.ftc.teamcode.util.field.Context.balance;
import static org.firstinspires.ftc.teamcode.util.field.Context.packet;

@TeleOp
public class DepositTest extends ModuleTest {
    Intake intake;
    Deposit deposit;
    GamepadEx primary;
    Lift.Level defaultDepositState = Lift.Level.LEVEL3;
    ToggleButtonReader tippedAway, tippedToward, levelIncrement, levelDecrement, farDeposit;

    @Override
    public void initialize() {
        intake = new Intake(hardwareMap);
        deposit = new Deposit(hardwareMap, intake);
        primary = new GamepadEx(gamepad1);
        setModules(deposit, intake);
        setKeyReaders(
                levelDecrement = new ToggleButtonReader(primary, GamepadKeys.Button.DPAD_DOWN),
                levelIncrement = new ToggleButtonReader(primary, GamepadKeys.Button.DPAD_UP),
                tippedAway = new ToggleButtonReader(primary, GamepadKeys.Button.LEFT_BUMPER),
                tippedToward = new ToggleButtonReader(primary, GamepadKeys.Button.RIGHT_BUMPER),
                farDeposit = new ToggleButtonReader(primary, GamepadKeys.Button.X)
        );
    }

    @Override
    public void update() {
        packet.put("isLoaded", Platform.isLoaded);
        packet.put("farDeposit", Deposit.farDeposit);
        packet.put("balance", balance);
        packet.put("LB", tippedAway.getState());
        packet.put("RB", tippedToward.getState());
        intake.setPower(gamepad1.right_trigger + gamepad1.left_trigger);
        if (gamepad1.b) {
            Platform.isLoaded = true;
            //deposit.platform.prepPlatform(deposit.getDefaultState());
        }
        if (farDeposit.wasJustPressed()) {
            Deposit.farDeposit = !Deposit.farDeposit;
        }
        if (intake.getContainsBlock() && intake.getState() == Intake.State.OUT) {
            gamepad1.rumble(500);
            gamepad2.rumble(500);
        }
        if (gamepad1.a) {
            deposit.dump();
        }
        if (tippedAway.isDown() && tippedToward.isDown()) {
            balance = Balance.BALANCED;
        } else if (tippedAway.isDown()) {
            balance = Balance.AWAY;
        } else if (tippedToward.isDown()) {
            balance = Balance.TOWARD;
        }
        if (levelIncrement.wasJustPressed()) {
            switch (defaultDepositState) {
                case LEVEL2:
                    defaultDepositState = Lift.Level.LEVEL3;
                    break;
                case LEVEL1:
                    defaultDepositState = Lift.Level.LEVEL2;
                    break;
            }
            deposit.setDefaultLevel(defaultDepositState);
        } else if (levelDecrement.wasJustPressed()) {
            switch (defaultDepositState) {
                case LEVEL3:
                    defaultDepositState = Lift.Level.LEVEL2;
                    break;
                case LEVEL2:
                    defaultDepositState = Lift.Level.LEVEL1;
                    break;
            }
            deposit.setDefaultLevel(defaultDepositState);
        }
    }
}
