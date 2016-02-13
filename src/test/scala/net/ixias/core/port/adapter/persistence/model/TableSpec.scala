/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.model

import org.joda.time.DateTime
import slick.driver.JdbcProfile

import org.specs2.mutable.Specification

// 定義
//~~~~~~
/** レコード定義 */
case class UserTableRecord(
  val uid:       String,
  val name:      Option[String],
  val email:     Option[String],
  val updatedAt: DateTime = new DateTime,
  val createdAt: DateTime = new DateTime
)

/** テーブル定義 */
trait UserTable[P <: JdbcProfile] extends Table[UserTableRecord, P] {

  /** クエリー定義 */
  object TableQuery extends BasicTableQuery(new Table(_)) {
  }

  /** テーブル定義 */
  class Table(tag: Tag) extends BasicTable(tag, "user") {
    import api._
    def id        = column[String]         ("id",         O.AsciiChar8, O.PrimaryKey)
    def name      = column[Option[String]] ("id",         O.AsciiChar8, O.PrimaryKey)
    def email     = column[Option[String]] ("id",         O.AsciiChar8, O.PrimaryKey)
    def updatedAt = column[DateTime]       ("updated_at", O.TsCurrent)
    def createdAt = column[DateTime]       ("created_at", O.Ts)
    def * = (id, name, email, updatedAt, createdAt) <> (UserTableRecord.tupled, UserTableRecord.unapply)
  }
}

// テスト
//~~~~~~~~
class TableSpec extends Specification {
  def unlift[A, B](f: A => Option[B]): A => B = Function.unlift(f)

  "Table" should {
    "declare" in {
      val exists = 1
      val user = UserTableRecord("1001", Some("name"), Some("name@nextbeat.net"))
      println(unlift(UserTableRecord.unapply)(user))
      exists must_== 1
    }
  }
}
