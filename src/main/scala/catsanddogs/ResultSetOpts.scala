package catsanddogs

import java.sql.ResultSet


class ResultSetOpts(rs: ResultSet) {

  def asAll[T](implicit resultSetReads: ResultSetReads[T]): Seq[T] = {
    def go(rrs:ResultSet):Seq[T] = {
      if (rrs.next()) {
        resultSetReads.read(rrs) +: go(rrs)
      }
      else {
        Nil
      }
    }
    go(rs)
  }

}


trait ResultSetReads[T]{
  def read(rs: ResultSet): T
}

object ResultSetOptsImplicit {
  implicit def toResultSetOpts(resultSet: ResultSet): ResultSetOpts = new ResultSetOpts(resultSet)
}
