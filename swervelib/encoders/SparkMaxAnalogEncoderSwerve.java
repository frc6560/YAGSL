package com.frc3481.swervelib.encoders;

import com.revrobotics.REVLibError;
import com.revrobotics.spark.SparkAnalogSensor;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.ClosedLoopConfig.FeedbackSensor;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import java.util.function.Supplier;
import com.frc3481.swervelib.motors.SparkMaxBrushedMotorSwerve;
import com.frc3481.swervelib.motors.SparkMaxSwerve;
import com.frc3481.swervelib.motors.SwerveMotor;

/**
 * SparkMax absolute encoder, attached through the data port analog pin.
 */
public class SparkMaxAnalogEncoderSwerve extends SwerveAbsoluteEncoder
{

  /**
   * {@link swervelib.motors.SparkMaxSwerve} or {@link swervelib.motors.SparkMaxBrushedMotorSwerve} object.
   */
  private final SwerveMotor       sparkMax;
  /**
   * The {@link SparkAnalogSensor} representing the duty cycle encoder attached to the SparkMax analog port.
   */
  public        SparkAnalogSensor encoder;
  /**
   * An {@link Alert} for if there is a failure configuring the encoder.
   */
  private       Alert             failureConfiguring;
  /**
   * An {@link Alert} for if the absolute encoder does not support integrated offsets.
   */
  private       Alert             doesNotSupportIntegratedOffsets;

  /**
   * Create the {@link SparkMaxAnalogEncoderSwerve} object as a analog sensor from the {@link SparkMax} motor data port
   * analog pin.
   *
   * @param motor      Motor to create the encoder from.
   * @param maxVoltage Maximum voltage for analog input reading.
   */
  public SparkMaxAnalogEncoderSwerve(SwerveMotor motor, double maxVoltage)
  {
    if (motor.getMotor() instanceof SparkMax)
    {
      sparkMax = motor;
      encoder = ((SparkMax) motor.getMotor()).getAnalog();
      setConversionFactor(360.0 / maxVoltage);
    } else
    {
      throw new RuntimeException("Motor given to instantiate SparkMaxEncoder is not a CANSparkMax");
    }
    failureConfiguring = new Alert(
        "Encoders",
        "Failure configuring SparkMax Analog Encoder",
        AlertType.kWarning);
    doesNotSupportIntegratedOffsets = new Alert(
        "Encoders",
        "SparkMax Analog Sensors do not support integrated offsets",
        AlertType.kWarning);

  }

  @Override
  public void close()
  {
    // SPARK MAX Analog encoder gets closed with the motor
    // I don't think an encoder getting closed should 
    // close the entire motor so i will keep this empty
    // sparkMax.close();
  }

  /**
   * Run the configuration until it succeeds or times out.
   *
   * @param config Lambda supplier returning the error state.
   */
  private void configureSparkMax(Supplier<REVLibError> config)
  {
    for (int i = 0; i < maximumRetries; i++)
    {
      if (config.get() == REVLibError.kOk)
      {
        return;
      }
    }
    failureConfiguring.set(true);
  }

  /**
   * Set the conversion factor of the {@link SparkMaxAnalogEncoderSwerve}.
   *
   * @param conversionFactor Position conversion factor from ticks to unit.
   */
  public void setConversionFactor(double conversionFactor)
  {
    SparkMaxConfig cfg = null;
    if (sparkMax instanceof SparkMaxSwerve)
    {
      cfg = ((SparkMaxSwerve) sparkMax).getConfig();

    } else if (sparkMax instanceof SparkMaxBrushedMotorSwerve)
    {
      cfg = ((SparkMaxBrushedMotorSwerve) sparkMax).getConfig();
    }
    if (cfg != null)
    {
      cfg.closedLoop.feedbackSensor(FeedbackSensor.kAnalogSensor);

      cfg.signals
          .analogVelocityAlwaysOn(true)
          .analogVoltageAlwaysOn(true)
          .analogPositionAlwaysOn(true)
          .analogVoltagePeriodMs(20)
          .analogPositionPeriodMs(20)
          .analogVelocityPeriodMs(20);

      cfg.analogSensor
          .positionConversionFactor(conversionFactor)
          .velocityConversionFactor(conversionFactor / 60);
    }
    if (sparkMax instanceof SparkMaxSwerve)
    {
      ((SparkMaxSwerve) sparkMax).updateConfig(cfg);
    } else if (sparkMax instanceof SparkMaxBrushedMotorSwerve)
    {
      ((SparkMaxBrushedMotorSwerve) sparkMax).updateConfig(cfg);
    }

  }


  /**
   * Reset the encoder to factory defaults.
   */
  @Override
  public void factoryDefault()
  {
    // Do nothing
  }

  /**
   * Clear sticky faults on the encoder.
   */
  @Override
  public void clearStickyFaults()
  {
    // Do nothing
  }

  /**
   * Configure the absolute encoder to read from [0, 360) per second.
   *
   * @param inverted Whether the encoder is inverted.
   */
  @Override
  public void configure(boolean inverted)
  {
    if (sparkMax instanceof SparkMaxSwerve)
    {
      SparkMaxConfig cfg = ((SparkMaxSwerve) sparkMax).getConfig();
      cfg.analogSensor.inverted(inverted);
      ((SparkMaxSwerve) sparkMax).updateConfig(cfg);
    } else if (sparkMax instanceof SparkMaxBrushedMotorSwerve)
    {
      SparkMaxConfig cfg = ((SparkMaxBrushedMotorSwerve) sparkMax).getConfig();
      cfg.analogSensor.inverted(inverted);
      ((SparkMaxBrushedMotorSwerve) sparkMax).updateConfig(cfg);
    }
  }

  /**
   * Get the absolute position of the encoder.
   *
   * @return Absolute position in degrees from [0, 360).
   */
  @Override
  public double getAbsolutePosition()
  {
    return encoder.getPosition();
  }

  /**
   * Get the instantiated absolute encoder Object.
   *
   * @return Absolute encoder object.
   */
  @Override
  public Object getAbsoluteEncoder()
  {
    return encoder;
  }

  /**
   * Sets the Absolute Encoder offset at the Encoder Level.
   *
   * @param offset the offset the Absolute Encoder uses as the zero point.
   * @return if setting Absolute Encoder Offset was successful or not.
   */
  @Override
  public boolean setAbsoluteEncoderOffset(double offset)
  {
    doesNotSupportIntegratedOffsets.set(true);
    return false;
  }

  /**
   * Get the velocity in degrees/sec.
   *
   * @return velocity in degrees/sec.
   */
  @Override
  public double getVelocity()
  {
    return encoder.getVelocity();
  }
}
