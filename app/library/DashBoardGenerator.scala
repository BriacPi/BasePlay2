package library

import models.{Status, SuspectRow, DashBoard, Hierarchy}

object DashBoardGenerator {
  def createDashBoard(hierarchy:Hierarchy,caisse: String = "all", groupe: String = "all", agence: String = "all", pdv: String = "all"): DashBoard = {
    val suspectRows = SuspectRow.all().filter(suspectRow => {
      (caisse == "all" || caisse==suspectRow.caisse) &&(groupe == "all" || groupe==suspectRow.groupe) &&
        (agence == "all" || agence==suspectRow.agence) &&(pdv == "all" || pdv==suspectRow.pdv)
    }
    )
    val leaderBoard = createLeaderBoard(hierarchy,caisse, groupe, agence, pdv,suspectRows)
    val statusChart = createStatusChart(caisse, groupe, agence, pdv,suspectRows)
    val natureChart = createNatureChart(caisse, groupe, agence, pdv,suspectRows)

    DashBoard(leaderBoard,statusChart,natureChart)
  }

  def createLeaderBoard(hierarchy:Hierarchy,caisse: String, groupe: String , agence: String , pdv: String ,suspectRows:List[SuspectRow]): List[(String,Long)] = {
    if (hierarchy==Hierarchy.Pdv) {
      List.empty[(String,Long)]
    } else if(hierarchy==Hierarchy.Agence){
      val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.pdv)
      underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList
    } else if(hierarchy==Hierarchy.Groupe){
      val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.agence)
      underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList
    } else if(hierarchy==Hierarchy.Caisse){
      val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.groupe)
      underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList
    } else {
      val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.caisse)
      underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList
    }
  }
}
