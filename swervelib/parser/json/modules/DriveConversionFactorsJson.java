package com.frc3481.swervelib.parser.json.modules;

import edu.wpi.first.math.util.Units;
import com.frc3481.swervelib.math.SwerveMath;

/**
 * Drive motor composite JSON parse class.
 */
public class DriveConversionFactorsJson
{

  /**
   * Gear ratio for the drive motor rotations to turn the wheel 1 complete rotation.
   */
  public double gearRatio;
  /**
   * Diameter of the wheel in inches.
   */
  public double diameter;
  /**
   * Calculated conversion factor.
   */
  public double factor = 0;

  /**
   * Calculate the drive conversion factor.
   *
   * @return Drive conversion factor, if factor isn't set.
   */
  public double calculate()
  {
    if (factor == 0)
    {
      factor = SwerveMath.calculateMetersPerRotation(Units.inchesToMeters(this.diameter), this.gearRatio);
    }
    return factor;
  }
}
