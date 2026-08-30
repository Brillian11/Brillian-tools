package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.DashboardRepository
import com.example.data.repository.MaterialRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SyncRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.ToolLogRepository
import com.example.ui.screens.civil.AggregateSieveViewModel
import com.example.ui.screens.civil.BeamDeflectionViewModel
import com.example.ui.screens.civil.ConcreteVolumeViewModel
import com.example.ui.screens.civil.CutFillEarthworkViewModel
import com.example.ui.screens.civil.EarthworkGradeViewModel
import com.example.ui.screens.civil.EquipmentHaulingViewModel
import com.example.ui.screens.civil.FramingRoofingViewModel
import com.example.ui.screens.civil.MasonryMortarViewModel
import com.example.ui.screens.civil.MeteorologyViewModel
import com.example.ui.screens.civil.ParabolicFocusViewModel
import com.example.ui.screens.civil.RebarEstimatorViewModel
import com.example.ui.screens.civil.RetainingWallSizerViewModel
import com.example.ui.screens.civil.SlopeDrainageViewModel
import com.example.ui.screens.civil.SoilAsphaltViewModel
import com.example.ui.screens.civil.StationingCogoViewModel
import com.example.ui.screens.civil.StormwaterRationalViewModel
import com.example.ui.screens.customize.CustomizeDashboardViewModel
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.electrical.BoxFillCapacityViewModel
import com.example.ui.screens.electrical.BreakerPanelViewModel
import com.example.ui.screens.electrical.ConduitBenderViewModel
import com.example.ui.screens.electrical.ConduitFillViewModel
import com.example.ui.screens.electrical.CurrentLoopScalingViewModel
import com.example.ui.screens.electrical.IndustrialMotorFlaViewModel
import com.example.ui.screens.electrical.LedDriverViewModel
import com.example.ui.screens.electrical.OhmsLawViewModel
import com.example.ui.screens.electrical.PowerFactorCorrectionViewModel
import com.example.ui.screens.electrical.ResistorColorCodeViewModel
import com.example.ui.screens.electrical.SolarBatterySizerViewModel
import com.example.ui.screens.electrical.VoltageDropViewModel
import com.example.ui.screens.inventory.MaterialInventoryViewModel
import com.example.ui.screens.ir.JobsiteIrRemoteViewModel
import com.example.ui.screens.maintenance.DrywallStudViewModel
import com.example.ui.screens.maintenance.HvacLoadViewModel
import com.example.ui.screens.maintenance.PaintCoverageViewModel
import com.example.ui.screens.maintenance.PipeSizingViewModel
import com.example.ui.screens.maintenance.TileGroutViewModel
import com.example.ui.screens.safety.ChemicalMsdsViewModel
import com.example.ui.screens.safety.SafetyChecklistViewModel
import com.example.ui.screens.sensors.ArAreaCalculatorViewModel
import com.example.ui.screens.sensors.ArMeasurementViewModel
import com.example.ui.screens.sensors.BarometricAltimeterViewModel
import com.example.ui.screens.sensors.BleMultimeterViewModel
import com.example.ui.screens.sensors.CompassViewModel
import com.example.ui.screens.sensors.DecibelMeterViewModel
import com.example.ui.screens.sensors.DigitalLevelViewModel
import com.example.ui.screens.sensors.CalculatorViewModel
import com.example.ui.screens.sensors.FractionalCalculatorViewModel
import com.example.ui.screens.sensors.PsychrometricViewModel
import com.example.ui.screens.sensors.RefrigerantViewModel
import com.example.ui.screens.sensors.DuctSizerViewModel
import com.example.ui.screens.sensors.ExpansionTankViewModel
import com.example.ui.screens.sensors.SlingAngleViewModel
import com.example.ui.screens.painting.PaintingCoatingStudioViewModel
import com.example.ui.screens.metalworks.MetalworksStudioViewModel
import com.example.ui.screens.sensors.LaserMeasureViewModel
import com.example.ui.screens.sensors.LuxMeterViewModel
import com.example.ui.screens.sensors.PlumbBobViewModel
import com.example.ui.screens.sensors.StrobeTachometerViewModel
import com.example.ui.screens.sensors.StudDetectorViewModel
import com.example.ui.screens.sensors.SunPathTrackerViewModel
import com.example.ui.screens.sensors.ThermalCameraViewModel
import com.example.ui.screens.sensors.UsbEndoscopeViewModel
import com.example.ui.screens.sensors.UsbProCameraViewModel
import com.example.ui.screens.sensors.QrCodeScannerViewModel
import com.example.ui.screens.sensors.VibrationAnalyzerViewModel
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.sync.SyncQueueViewModel
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.tools.ColorDevToolsViewModel
import com.example.ui.screens.tools.FocusTimerViewModel
import com.example.ui.screens.tools.QuickNotesViewModel
import com.example.ui.screens.tools.TaskChecklistViewModel
import com.example.ui.screens.tools.UnitConverterViewModel
import com.example.ui.screens.woodworking.BladeSpeedViewModel
import com.example.ui.screens.woodworking.BoardFootageViewModel
import com.example.ui.screens.woodworking.CompoundMiterViewModel
import com.example.ui.screens.woodworking.CutlistOptimizerViewModel
import com.example.ui.screens.woodworking.DadoStepOverViewModel
import com.example.ui.screens.woodworking.DrillTapChartViewModel
import com.example.ui.screens.woodworking.GrainMatchingViewModel
import com.example.ui.screens.woodworking.JoinerySpacingViewModel
import com.example.ui.screens.woodworking.KerfBendingViewModel
import com.example.ui.screens.woodworking.RafterCalculatorViewModel
import com.example.ui.screens.woodworking.SagulatorViewModel
import com.example.ui.screens.woodworking.SegmentedTurningViewModel
import com.example.ui.screens.woodworking.StairLayoutViewModel
import com.example.ui.screens.woodworking.WoodMoistureViewModel
import com.example.ui.screens.woodworking.WoodSpeciesStudioViewModel
import com.example.ui.screens.work.WorkTrackingViewModel
import com.example.data.repository.WorkTrackingRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val db by lazy { AppDatabase.getInstance(context) }
    private val dashboardRepository by lazy { DashboardRepository(db.dashboardDao()) }
    private val taskRepository by lazy { TaskRepository(db.quickTaskDao(), db.syncQueueDao()) }
    private val noteRepository by lazy { NoteRepository(db.quickNoteDao(), db.syncQueueDao()) }
    private val toolLogRepository by lazy { ToolLogRepository(db.toolLogDao(), noteRepository) }
    private val syncRepository by lazy { SyncRepository(db.syncQueueDao(), db.quickTaskDao(), db.quickNoteDao()) }
    private val materialRepository by lazy { MaterialRepository(db.materialDao()) }
    val settingsRepository by lazy { SettingsRepository(context) }
    private val workTrackingRepository by lazy { WorkTrackingRepository(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext as Application
        return when {
            modelClass.isAssignableFrom(WorkTrackingViewModel::class.java) -> {
                WorkTrackingViewModel(context, workTrackingRepository, settingsRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(MeteorologyViewModel::class.java) -> {
                MeteorologyViewModel() as T
            }
            modelClass.isAssignableFrom(CompassViewModel::class.java) -> {
                CompassViewModel(app) as T
            }
            modelClass.isAssignableFrom(ParabolicFocusViewModel::class.java) -> {
                ParabolicFocusViewModel() as T
            }
            modelClass.isAssignableFrom(ArMeasurementViewModel::class.java) -> {
                ArMeasurementViewModel(app) as T
            }
            modelClass.isAssignableFrom(ArAreaCalculatorViewModel::class.java) -> {
                ArAreaCalculatorViewModel(app) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(dashboardRepository, taskRepository, noteRepository, syncRepository, toolLogRepository, settingsRepository) as T
            }
            modelClass.isAssignableFrom(CustomizeDashboardViewModel::class.java) -> {
                CustomizeDashboardViewModel(dashboardRepository) as T
            }
            modelClass.isAssignableFrom(BoardFootageViewModel::class.java) -> {
                BoardFootageViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(StairLayoutViewModel::class.java) -> {
                StairLayoutViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(RafterCalculatorViewModel::class.java) -> {
                RafterCalculatorViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(CompoundMiterViewModel::class.java) -> {
                CompoundMiterViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(WoodMoistureViewModel::class.java) -> {
                WoodMoistureViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(WoodSpeciesStudioViewModel::class.java) -> {
                WoodSpeciesStudioViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(JoinerySpacingViewModel::class.java) -> {
                JoinerySpacingViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SegmentedTurningViewModel::class.java) -> {
                SegmentedTurningViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SagulatorViewModel::class.java) -> {
                SagulatorViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(GrainMatchingViewModel::class.java) -> {
                GrainMatchingViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(DrillTapChartViewModel::class.java) -> {
                DrillTapChartViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(BladeSpeedViewModel::class.java) -> {
                BladeSpeedViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(OhmsLawViewModel::class.java) -> {
                OhmsLawViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(VoltageDropViewModel::class.java) -> {
                VoltageDropViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ConduitFillViewModel::class.java) -> {
                ConduitFillViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ConduitBenderViewModel::class.java) -> {
                ConduitBenderViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ResistorColorCodeViewModel::class.java) -> {
                ResistorColorCodeViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(BreakerPanelViewModel::class.java) -> {
                BreakerPanelViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(LedDriverViewModel::class.java) -> {
                LedDriverViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(BoxFillCapacityViewModel::class.java) -> {
                BoxFillCapacityViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(IndustrialMotorFlaViewModel::class.java) -> {
                IndustrialMotorFlaViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SolarBatterySizerViewModel::class.java) -> {
                SolarBatterySizerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(CurrentLoopScalingViewModel::class.java) -> {
                CurrentLoopScalingViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PowerFactorCorrectionViewModel::class.java) -> {
                PowerFactorCorrectionViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PipeSizingViewModel::class.java) -> {
                PipeSizingViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(HvacLoadViewModel::class.java) -> {
                HvacLoadViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(TileGroutViewModel::class.java) -> {
                TileGroutViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PaintCoverageViewModel::class.java) -> {
                PaintCoverageViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(DrywallStudViewModel::class.java) -> {
                DrywallStudViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SafetyChecklistViewModel::class.java) -> {
                SafetyChecklistViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ChemicalMsdsViewModel::class.java) -> {
                ChemicalMsdsViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(KerfBendingViewModel::class.java) -> {
                KerfBendingViewModel() as T
            }
            modelClass.isAssignableFrom(CutlistOptimizerViewModel::class.java) -> {
                CutlistOptimizerViewModel(materialRepository) as T
            }
            modelClass.isAssignableFrom(DadoStepOverViewModel::class.java) -> {
                DadoStepOverViewModel() as T
            }
            modelClass.isAssignableFrom(BeamDeflectionViewModel::class.java) -> {
                BeamDeflectionViewModel() as T
            }
            modelClass.isAssignableFrom(ConcreteVolumeViewModel::class.java) -> {
                ConcreteVolumeViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(RebarEstimatorViewModel::class.java) -> {
                RebarEstimatorViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(CutFillEarthworkViewModel::class.java) -> {
                CutFillEarthworkViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(MasonryMortarViewModel::class.java) -> {
                MasonryMortarViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SlopeDrainageViewModel::class.java) -> {
                SlopeDrainageViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SoilAsphaltViewModel::class.java) -> {
                SoilAsphaltViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(EarthworkGradeViewModel::class.java) -> {
                EarthworkGradeViewModel() as T
            }
            modelClass.isAssignableFrom(FramingRoofingViewModel::class.java) -> {
                FramingRoofingViewModel() as T
            }
            modelClass.isAssignableFrom(DigitalLevelViewModel::class.java) -> {
                DigitalLevelViewModel(app) as T
            }
            modelClass.isAssignableFrom(DecibelMeterViewModel::class.java) -> {
                DecibelMeterViewModel() as T
            }
            modelClass.isAssignableFrom(StrobeTachometerViewModel::class.java) -> {
                StrobeTachometerViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ThermalCameraViewModel::class.java) -> {
                ThermalCameraViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(UsbEndoscopeViewModel::class.java) -> {
                UsbEndoscopeViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(UsbProCameraViewModel::class.java) -> {
                UsbProCameraViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(QrCodeScannerViewModel::class.java) -> {
                QrCodeScannerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(VibrationAnalyzerViewModel::class.java) -> {
                VibrationAnalyzerViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(BleMultimeterViewModel::class.java) -> {
                BleMultimeterViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SunPathTrackerViewModel::class.java) -> {
                SunPathTrackerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(BarometricAltimeterViewModel::class.java) -> {
                BarometricAltimeterViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(LuxMeterViewModel::class.java) -> {
                LuxMeterViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(FractionalCalculatorViewModel::class.java) -> {
                FractionalCalculatorViewModel() as T
            }
            modelClass.isAssignableFrom(CalculatorViewModel::class.java) -> {
                CalculatorViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PlumbBobViewModel::class.java) -> {
                PlumbBobViewModel(app) as T
            }
            modelClass.isAssignableFrom(StudDetectorViewModel::class.java) -> {
                StudDetectorViewModel(app) as T
            }
            modelClass.isAssignableFrom(LaserMeasureViewModel::class.java) -> {
                LaserMeasureViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(MaterialInventoryViewModel::class.java) -> {
                MaterialInventoryViewModel(materialRepository) as T
            }
            modelClass.isAssignableFrom(FocusTimerViewModel::class.java) -> {
                FocusTimerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(UnitConverterViewModel::class.java) -> {
                UnitConverterViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ColorDevToolsViewModel::class.java) -> {
                ColorDevToolsViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(QuickNotesViewModel::class.java) -> {
                QuickNotesViewModel(noteRepository) as T
            }
            modelClass.isAssignableFrom(TaskChecklistViewModel::class.java) -> {
                TaskChecklistViewModel(taskRepository) as T
            }
            modelClass.isAssignableFrom(SyncQueueViewModel::class.java) -> {
                SyncQueueViewModel(syncRepository) as T
            }
            modelClass.isAssignableFrom(JobsiteIrRemoteViewModel::class.java) -> {
                JobsiteIrRemoteViewModel(app, toolLogRepository) as T
            }
            modelClass.isAssignableFrom(StationingCogoViewModel::class.java) -> {
                StationingCogoViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(RetainingWallSizerViewModel::class.java) -> {
                RetainingWallSizerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(AggregateSieveViewModel::class.java) -> {
                AggregateSieveViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(EquipmentHaulingViewModel::class.java) -> {
                EquipmentHaulingViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(StormwaterRationalViewModel::class.java) -> {
                StormwaterRationalViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PsychrometricViewModel::class.java) -> {
                PsychrometricViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(RefrigerantViewModel::class.java) -> {
                RefrigerantViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(DuctSizerViewModel::class.java) -> {
                DuctSizerViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(ExpansionTankViewModel::class.java) -> {
                ExpansionTankViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(SlingAngleViewModel::class.java) -> {
                SlingAngleViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(PaintingCoatingStudioViewModel::class.java) -> {
                PaintingCoatingStudioViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(MetalworksStudioViewModel::class.java) -> {
                MetalworksStudioViewModel(toolLogRepository) as T
            }
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                LibraryViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
