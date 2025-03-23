package com.frc3481.swervelib.parser.json;

import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.util.Units;
import com.frc3481.swervelib.encoders.SparkMaxEncoderSwerve;
import com.frc3481.swervelib.encoders.SwerveAbsoluteEncoder;
import com.frc3481.swervelib.encoders.ThriftyNovaEncoderSwerve;
import com.frc3481.swervelib.motors.SwerveMotor;
import com.frc3481.swervelib.motors.ThriftyNovaSwerve;
import com.frc3481.swervelib.parser.PIDFConfig;
import com.frc3481.swervelib.parser.SwerveModuleConfiguration;
import com.frc3481.swervelib.parser.SwerveModulePhysicalCharacteristics;
import com.frc3481.swervelib.parser.json.modules.BoolMotorJson;
import com.frc3481.swervelib.parser.json.modules.ConversionFactorsJson;
import com.frc3481.swervelib.parser.json.modules.LocationJson;

/**
 * {@link swervelib.SwerveModule} JSON parsed class. Used to access the JSON data.
 */
public class ModuleJson
{

  /**
   * Drive motor device configuration.
   */
  public DeviceJson            drive;
  /**
   * Angle motor device configuration.
   */
  public DeviceJson            angle;
  /**
   * Conversion Factors composition. Auto-calculates the conversion factors.
   */
  public ConversionFactorsJson conversionFactors       = new ConversionFactorsJson();
  /**
   * Absolute encoder device configuration.
   */
  public DeviceJson            encoder;
  /**
   * Defines which motors are inverted.
   */
  public BoolMotorJson         inverted;
  /**
   * Absolute encoder offset from 0 in degrees.
   */
  public double                absoluteEncoderOffset;
  /**
   * Absolute encoder inversion state.
   */
  public boolean               absoluteEncoderInverted = false;
  /**
   * The location of the swerve module from the center of the robot in inches.
   */
  public LocationJson          location;
  /**
   * Should do cosine compensation when not pointing correct direction;.
   */
  public boolean               useCosineCompensator    = true;

  /**
   * Create the swerve module configuration based off of parsed data.
   *
   * @param anglePIDF               The PIDF values for the angle motor.
   * @param velocityPIDF            The velocity PIDF values for the drive motor.
   * @param physicalCharacteristics Physical characteristics of the swerve module.
   * @param name                    Module json filename.
   * @return {@link SwerveModuleConfiguration} based on the provided data and parsed data.
   */
  public SwerveModuleConfiguration createModuleConfiguration(
      PIDFConfig anglePIDF,
      PIDFConfig velocityPIDF,
      SwerveModulePhysicalCharacteristics physicalCharacteristics,
      String name)
  {
    SwerveMotor           angleMotor = angle.createMotor(false);
    SwerveAbsoluteEncoder absEncoder = encoder.createEncoder(angleMotor);

    //Throw an error if module locations are improperly set
    if (location.front == 0 && location.left == 0)
    {
      throw new RuntimeException("Improper Module Location Settings!\n" +
                                 "Your module location is set to 0 for both 'front' and 'left' values.\n" +
                                 "Set the distance from the center of the robot to the center of the wheel in your module JSON file!");
    }

    // Set the conversion factors to null if they are both 0.
    if (!conversionFactors.works() && physicalCharacteristics.conversionFactor == null)
    {
      throw new RuntimeException("No Conversion Factor configured! Please create SwerveDrive using \n" +
                                 "SwerveParser.createSwerveDrive(driveFeedforward, maxSpeed, angleMotorConversionFactor, driveMotorConversion)\n" +
                                 "OR\n" +
                                 "SwerveParser.createSwerveDrive(maxSpeed, angleMotorConversionFactor, driveMotorConversion)\n" +
                                 "OR\n" +
                                 "Set the conversion factor in physicalproperties.json OR the module JSON file." +
                                 "REMEMBER: You can calculate the conversion factors using SwerveMath.calculateMetersPerRotation AND SwerveMath.calculateDegreesPerSteeringRotation\n");
    } else if (physicalCharacteristics.conversionFactor.works() && !conversionFactors.works())
    {
      conversionFactors = physicalCharacteristics.conversionFactor;
    } else if (physicalCharacteristics.conversionFactor.works())
    // If both are defined, override 0 with the physical characterstics input.
    {
      conversionFactors.angle = conversionFactors.isAngleEmpty() ? physicalCharacteristics.conversionFactor.angle
                                                                 : conversionFactors.angle;
      conversionFactors.drive = conversionFactors.isDriveEmpty() ? physicalCharacteristics.conversionFactor.drive
                                                                 : conversionFactors.drive;
    }

    if (conversionFactors.isDriveEmpty() || conversionFactors.isAngleEmpty())
    {
      throw new RuntimeException(
          "Conversion factors cannot be 0, please configure conversion factors in physicalproperties.json or the module JSON files.");
    }

    // Backwards compatibility, auto-optimization.
    if (conversionFactors.angle.factor == 360 && absEncoder != null &&
        (absEncoder instanceof SparkMaxEncoderSwerve && angleMotor.getMotor() instanceof SparkMax))
    {
      angleMotor.setAbsoluteEncoder(absEncoder);
    } else if ((absEncoder instanceof ThriftyNovaEncoderSwerve && angleMotor instanceof ThriftyNovaSwerve))
    {
      angleMotor.setAbsoluteEncoder(absEncoder);
    }

    return new SwerveModuleConfiguration(
        drive.createMotor(true),
        angleMotor,
        conversionFactors,
        absEncoder,
        absoluteEncoderOffset,
        Units.inchesToMeters(Math.round(location.front)),
        Units.inchesToMeters(Math.round(location.left)),
        anglePIDF,
        velocityPIDF,
        physicalCharacteristics,
        absoluteEncoderInverted,
        inverted.drive,
        inverted.angle,
        name.replaceAll("\\.json", ""),
        useCosineCompensator);
  }
}
