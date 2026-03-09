<template>
    <q-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
        <q-card style="min-width: 400px" class="cardTerms">
            <q-card-section class="row items-center q-pt-lg">
                <div class="text-h6">Términos y condiciones</div>
                <q-space />
                <q-btn icon="close" flat round dense v-close-popup />
            </q-card-section>
            <q-separator class="separator"/>
            <q-card-section class="q-pt-md">
                <p class="pTerms">
                    Al registrarte, aceptas nuestros términos y condiciones. Por favor, lee detenidamente antes de continuar.
                </p>
                <ul class="listTerms">
                    <li>1. Aceptación Al registrarte y usar nuestra plataforma, aceptas estos términos. Si no estás de acuerdo, no utilices el servicio.</li>
                    <li>2. Cuenta de usuario Eres responsable de mantener la confidencialidad de tu cuenta y contraseña. Debes proporcionar información veraz durante el registro.</li>
                    <li>3. Acceso a los cursos Al adquirir un curso obtienes una licencia personal, intransferible y no exclusiva. El contenido es para uso individual y no puede compartirse ni redistribuirse.</li>
                    <li>4. Pagos y reembolsos Los pagos se procesan de forma segura. Puedes solicitar reembolso dentro de los 14 días siguientes a la compra, siempre que no hayas completado más del 20% del curso.</li>
                    <li>5. Propiedad intelectual Todo el contenido de la plataforma (vídeos, textos, materiales) es propiedad de CourselyLabs o de sus instructores. Queda prohibida su reproducción sin autorización expresa.</li>
                    <li>6. Conducta del usuario Está prohibido compartir contenido ofensivo, hacer un uso fraudulento de la plataforma o intentar acceder a cuentas ajenas.</li>
                    <li>7. Modificaciones Nos reservamos el derecho de modificar estos términos en cualquier momento. Se notificará a los usuarios de cambios relevantes por correo electrónico.</li>
                    <li>8. Contacto Para cualquier consulta: support@courselylabs.com</li>
                </ul>
            </q-card-section>

            <q-card-actions align="right" class="btnsTerms">
                <q-btn flat label="Cancelar" color="primary" v-close-popup />
                <q-btn label="Acepto" color="primary" @click="$emit('accepted'); $emit('update:modelValue', false)" />
            </q-card-actions>
        </q-card>
    </q-dialog>
</template>

<script setup lang="ts">
defineProps(['modelValue'])
defineEmits(['update:modelValue', 'accepted'])
</script>

<style scoped>
.cardTerms{
    text-align: justify;
    height: 80vh;
    max-width: 675px;
}

.separator {
    width: 95%;
    margin: 0 auto;
}

.listTerms {
    margin: 0 ;
    padding: 0 30px;
    list-style-type: none;

    & li {
        margin-bottom: 10px;    
    }
}

.btnsTerms {
    padding: 0 40px;
}

.pTerms{
    margin-bottom: 30px;
}
</style>