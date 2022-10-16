package spj

import monix.newtypes

/** The reason for creating this simple wrapper is because it gives us control over how _we_ want to use newtypes in
  * this project. Given that any real project will have _hundreds_ of these, we definitely want to be able to alter all
  * of them at once.
  *
  * This very trait shows why. monix-newtypes does not depend on cats, for good reason. We on the other hand _do_ depend
  * on it, so it's absurd to keep working with the clunky signature of BuildFailure when we can just do stuff for any
  * F[_]: ApplicativeThrow
  */
abstract class SpjNewtypeValidated[Src] extends newtypes.NewtypeValidated[Src] {
  def refine[F[_]: ApplicativeThrow](value: Src): F[Type]

  override final def apply(value: Src): Either[newtypes.BuildFailure[Type], Type] =
    refine[Either[Throwable, *]](value).leftMap { failure => newtypes.BuildFailure[Type](failure.getMessage) }
}

abstract class SpjNewsubtypeValidated[Src] extends newtypes.NewsubtypeValidated[Src] {
  def refine[F[_]: ApplicativeThrow](value: Src): F[Type]

  override final def apply(value: Src): Either[newtypes.BuildFailure[Type], Type] =
    refine[Either[Throwable, *]](value).leftMap { failure => newtypes.BuildFailure[Type](failure.getMessage) }
}

/** The vast majority of newtypes are the "wrapped" ones, where you don't add validation, and you just want to do:
  * {{{
  *   ThisString("thisOne")
  *   ThatString("thatOne")
  * }}}
  * typing "Wrapped" as an extra for what we use as the default is annoying
  */
abstract class SpjNewtype[Src] extends newtypes.NewtypeWrapped[Src]

abstract class SpjNewsubtype[Src] extends newtypes.NewsubtypeWrapped[Src]
