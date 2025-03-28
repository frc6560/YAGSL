package com.frc3481.swervelib.parser.json;

import com.frc3481.swervelib.parser.PIDFConfig;

/**
 * {@link swervelib.SwerveModule} PID with Feedforward for the drive motor and angle motor.
 */
public class PIDFPropertiesJson
{

  /**
   * The PIDF with Integral Zone used for the drive motor.
   */
  public PIDFConfig drive;
  /**
   * The PIDF with Integral Zone used for the angle motor.
   */
  public PIDFConfig angle;
}
