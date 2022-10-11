package spj.api

trait SpjAPI[F[_]] {
  def upload(): F[APIResponse.Upload]
  def get(): F[APIResponse.Get]
  def validate(): F[APIResponse.Validate]
}
