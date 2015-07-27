package library

import models.{Nature, Status, SuspectRow}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class SuspectRowSpec extends Specification{



  "doctor-strange" should {

    "get the test data in the test Database" in  new WithApplication{
      SuspectRow.all().head mustEqual new SuspectRow(2,java.time.LocalDate.parse("2000-01-01"),"caisse","groupe","agence","pdv",
        "metric",Status.Solved,Nature.Abnormality, java.time.LocalDate.parse("2000-01-01"), "admin","comment")
    }


  }

}


