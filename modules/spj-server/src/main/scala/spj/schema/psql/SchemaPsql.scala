package spj.schema.psql

import spj.*
import spj.db.*
import spj.schema.*

final class SchemaPsql[F[_]] private (session: Session[F])(using F: MonadCancelThrow[F]) {
  def insert(id: SchemaId, jsonSchema: JsonSchema): F[Unit] =
    session.prepare(SchemaPsql.insertC).use { pc =>
      pc.execute(id ~ jsonSchema).void.adaptErr { case SqlState.UniqueViolation(_) =>
        Anomaly.conflict(what = "schemaId", value = id.value)
      }
    }
  def find(id: SchemaId): F[Option[JsonSchema]] =
    session.prepare(SchemaPsql.getQ).use(pc => pc.option(id))
}

object SchemaPsql {
  def apply[F[_]](session: Session[F])(using F: MonadCancelThrow[F]): SchemaPsql[F] = new SchemaPsql[F](session)

  private val json_schemas = const"""json_schemas"""
  private val id: Codec[SchemaId] = varchar(255).newtype[SchemaId]
  private val json_schema: Codec[JsonSchema] = jsonb[JsonSchema]

  private def insertC: Command[SchemaId ~ JsonSchema] =
    sql"""INSERT INTO $json_schemas VALUES ($id, $json_schema)""".command

  private def getQ: Query[SchemaId, JsonSchema] =
    sql"""SELECT json_schema FROM $json_schemas WHERE id = $id""".query(json_schema)
}
